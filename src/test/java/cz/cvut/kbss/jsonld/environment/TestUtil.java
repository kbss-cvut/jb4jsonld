package cz.cvut.kbss.jsonld.environment;

import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.model.*;

public class TestUtil {

    private TestUtil() {
        throw new AssertionError();
    }

    public static TypeMap getDefaultTypeMap() {
        final TypeMap tm = new TypeMap();
        tm.register(Vocabulary.EMPLOYEE, Employee.class);
        tm.register(Vocabulary.ORGANIZATION, Organization.class);
        tm.register(Vocabulary.PERSON, Person.class);
        tm.register(Vocabulary.STUDY, Study.class);
        tm.register(Vocabulary.USER, User.class);
        return tm;
    }
}
