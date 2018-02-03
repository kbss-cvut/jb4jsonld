package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NodeReferenceContextTest {

    @Mock
    private InstanceContext ownerMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void setIdentifierValueTransformsValueToTargetType() throws Exception {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(URL.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(new URL(id), ctx.getInstance());
    }

    @Test
    public void setIdentifierValueTransformsValueToTargetPropertyType() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(URI.create(id), ctx.instance);
    }

    @Test
    public void setIdentifierValueSetValueOfTypeStringWhenTargetIsString() {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(String.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(id, ctx.getInstance());
    }

    @Test
    public void closeAddsValueToOpenCollection() {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(URI.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        ctx.close();
        verify(ownerMock).addItem(URI.create(id));
    }

    @Test
    public void closeSetsFieldValueForCorrectProperty() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        ctx.close();
        verify(ownerMock).setFieldValue(field, URI.create(id));
    }

    @Test
    public void isPropertyMappedReturnsTrueForId() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        assertTrue(ctx.isPropertyMapped(JsonLd.ID));
        assertFalse(ctx.isPropertyMapped(Vocabulary.USERNAME));
    }
}