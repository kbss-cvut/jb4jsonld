package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer;

public class ContextBuildingTemporalSerializer extends TemporalSerializer {

    public ContextBuildingTemporalSerializer() {
        super(new ContextBuildingIsoDateTimeSerializer(), new ContextBuildingLocalDateSerializer(),
              new ContextBuildingTimeSerializer());
    }

    @Override
    public void configure(Configuration config) {
        assert config != null;
        this.dateTimeSerializer = config.is(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS) ?
                                  new ContextBuildingEpochBasedDateTimeSerializer() :
                                  new ContextBuildingIsoDateTimeSerializer();
        dateTimeSerializer.configure(config);
    }
}
