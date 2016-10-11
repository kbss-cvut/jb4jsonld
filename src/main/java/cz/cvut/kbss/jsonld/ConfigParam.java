package cz.cvut.kbss.jsonld;

public enum ConfigParam {

    IGNORE_UNKNOWN_PROPERTIES("ignoreUnknownProperties");

    private final String name;

    ConfigParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
