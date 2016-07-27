package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class AbstractNodeTest {

    @Mock
    JsonGenerator serializerMock;

    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
