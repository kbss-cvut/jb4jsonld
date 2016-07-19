package cz.cvut.kbss.jsonld.environment;

public class Vocabulary {

    public static final String PERSON = "http://onto.fel.cvut.cz/ontologies/ufo/Person";
    public static final String USER = "http://onto.fel.cvut.cz/ontologies/ufo/User";
    public static final String ORGANIZATION = "http://onto.fel.cvut.cz/ontologies/ufo/Organization";

    public static final String FIRST_NAME = "http://xmlns.com/foaf/0.1/firstName";
    public static final String LAST_NAME = "http://xmlns.com/foaf/0.1/lastName";
    public static final String USERNAME = "http://xmlns.com/foaf/0.1/accountName";
    public static final String DATE_CREATED = "http://purl.org/dc/terms/created";

    private Vocabulary() {
        throw new AssertionError();
    }
}
