package cz.cvut.kbss.jsonld.environment;

public class Vocabulary {

    public static final String PERSON = "http://onto.fel.cvut.cz/ontologies/ufo/Person";
    public static final String USER = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/User";
    public static final String EMPLOYEE = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/Employee";
    public static final String ORGANIZATION = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/Organization";

    public static final String FIRST_NAME = "http://xmlns.com/foaf/0.1/firstName";
    public static final String LAST_NAME = "http://xmlns.com/foaf/0.1/lastName";
    public static final String USERNAME = "http://xmlns.com/foaf/0.1/accountName";
    public static final String DATE_CREATED = "http://purl.org/dc/terms/created";
    public static final String IS_MEMBER_OF = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/isMemberOf";
    public static final String HAS_MEMBER = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/hasMember";
    public static final String BRAND = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/brand";

    private Vocabulary() {
        throw new AssertionError();
    }
}
