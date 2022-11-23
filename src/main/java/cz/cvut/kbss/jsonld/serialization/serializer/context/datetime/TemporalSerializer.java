package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;

public class TemporalSerializer extends cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer {

    public TemporalSerializer() {
        super(new IsoDateTimeSerializer(), new LocalDateSerializer(), new TimeSerializer());
    }

    @Override
    public void configure(Configuration config) {
        assert config != null;
        this.dateTimeSerializer = config.is(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS) ?
                                  new EpochBasedDateTimeSerializer() : new IsoDateTimeSerializer();
        dateTimeSerializer.configure(config);
    }
}
