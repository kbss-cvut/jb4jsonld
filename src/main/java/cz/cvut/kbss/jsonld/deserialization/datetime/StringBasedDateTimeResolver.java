package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdDateTimeMapper;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.Configurable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

class StringBasedDateTimeResolver implements Configurable {

    private DateTimeFormatter formatter;

    OffsetDateTime resolve(String value) {
        assert value != null;
        return formatter != null ? OffsetDateTime.parse(value, formatter) : XsdDateTimeMapper.map(value);
    }

    @Override
    public void configure(Configuration configuration) {
        if (configuration.has(ConfigParam.DATE_TIME_FORMAT)) {
            formatter = DateTimeFormatter.ofPattern(configuration.get(ConfigParam.DATE_TIME_FORMAT));
        }
    }
}
