package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonSerializer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class AbstractNodeTest {

    @Mock
    JsonSerializer serializerMock;

    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
