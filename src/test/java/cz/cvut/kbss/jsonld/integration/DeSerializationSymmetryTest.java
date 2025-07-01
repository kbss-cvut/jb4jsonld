package cz.cvut.kbss.jsonld.integration;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.JsonLdDeserializer;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithNumericAttributes;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

abstract class DeSerializationSymmetryTest {

    private BufferedJsonGenerator jsonWriter;

    private JsonLdSerializer serializer;

    private JsonLdDeserializer deserializer;

    @BeforeEach
    void setUp() {
        this.jsonWriter = new BufferedJsonGenerator();
        final Configuration configuration = new Configuration();
        configuration.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        configuration.set(ConfigParam.REQUIRE_ID, Boolean.toString(false));
        this.serializer = getSerializer(jsonWriter, configuration);
        this.deserializer = JsonLdDeserializer.createExpandedDeserializer(configuration);
    }

    abstract JsonLdSerializer getSerializer(BufferedJsonGenerator jsonWriter, Configuration configuration);

    @Test
    void serializationAndDeserializationAreCompatibleForTemporalAccessorValues() throws Exception {
        final TemporalEntity original = new TemporalEntity();
        original.initTemporalAccessorValues();
        final TemporalEntity result = serializeAndDeserialize(original);
        assertEquals(original.getOffsetDateTime(), result.getOffsetDateTime());
        assertEquals(original.getLocalDateTime(), result.getLocalDateTime());
        assertEquals(original.getZonedDateTime(), result.getZonedDateTime());
        assertEquals(original.getInstant(), result.getInstant());
        assertEquals(original.getTimestamp(), result.getTimestamp());
        assertEquals(original.getOffsetTime(), result.getOffsetTime());
        assertEquals(original.getLocalTime(), result.getLocalTime());
        assertEquals(original.getLocalDate(), result.getLocalDate());
    }

    private <T> T serializeAndDeserialize(T original) throws Exception {
        serializer.serialize(original);
        final String jsonLd = jsonWriter.getResult();
        return (T) deserializer.deserialize(TestUtil.parseAndExpand(jsonLd), original.getClass());
    }

    @Test
    void serializationAndDeserializationAreCompatibleForTemporalAmountValues() throws Exception {
        final TemporalEntity original = new TemporalEntity();
        original.initTemporalAmountValues();
        final TemporalEntity result = serializeAndDeserialize(original);
        assertEquals(original.getDuration(), result.getDuration());
        assertEquals(original.getPeriod(), result.getPeriod());
    }

    @Test
    void serializationAndDeserializationAreCompatibleForNumericValues() throws Exception {
        final ObjectWithNumericAttributes instance = new ObjectWithNumericAttributes(Generator.generateUri());
        instance.setDoubleValue(155.15);
        instance.setFloatValue(155.15f);
        instance.setLongValue(155L);
        instance.setShortValue((short) 155);
        instance.setIntValue(155);
        instance.setBigIntegerValue(BigInteger.valueOf(155L));
        instance.setBigDecimalValue(BigDecimal.valueOf(155.15));

        final ObjectWithNumericAttributes result = serializeAndDeserialize(instance);
        assertEquals(instance.getDoubleValue(), result.getDoubleValue());
        assertEquals(instance.getFloatValue(), result.getFloatValue());
        assertEquals(instance.getLongValue(), result.getLongValue());
        assertEquals(instance.getShortValue(), result.getShortValue());
        assertEquals(instance.getIntValue(), result.getIntValue());
        assertEquals(instance.getBigIntegerValue(), result.getBigIntegerValue());
        assertEquals(instance.getBigDecimalValue(), result.getBigDecimalValue());
    }

    @Test
    void serializationAndDeserializationAreCompatibleForBooleanValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(true);

        final User result = serializeAndDeserialize(user);
        assertEquals(user.getAdmin(), result.getAdmin());
    }

    @Test
    void serializationAndDeserializationHandlesBackwardReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Employee emp = Generator.generateEmployee();
        emp.setEmployer(org);
        org.addEmployee(emp);

        final Organization result = serializeAndDeserialize(org);
        assertEquals(1, result.getEmployees().size());
        assertEquals(result, result.getEmployees().iterator().next().getEmployer());
        assertSame(result, result.getEmployees().iterator().next().getEmployer());
    }
}
