# Java Binding for JSON-LD

[![Build Status](https://kbss.felk.cvut.cz/jenkins/buildStatus/icon?job=jaxb-jsonld)](https://kbss.felk.cvut.cz/jenkins/job/jaxb-jsonld)

Java Binding for JSON-LD (JB4JSON-LD) is a simple library for serialization of Java objects into JSON-LD and vice versa.

Note that this is the core, abstract implementation. For actual usage, a binding like 
[https://github.com/kbss-cvut/jb4jsonld-jackson](https://github.com/kbss-cvut/jb4jsonld-jackson)
has to be used.

More information can be found at [https://kbss.felk.cvut.cz/web/kbss/jb4json-ld](https://kbss.felk.cvut.cz/web/kbss/jb4json-ld).

## Usage

JB4JSON-LD is based on annotations from [JOPA](https://github.com/kbss-cvut/jopa), which enable POJO attributes
to be mapped to ontological constructs (i.e. to object, data or annotation properties) and Java classes to ontological
classes.

Use `@OWLDataProperty` to annotate data fields and `@OWLObjectProperty` to annotate fields referencing other mapped entities.

See [https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld](https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld) for
an executable example of JB4JSON-LD in action (together with Spring and Jackson).


## Example

### Java

```Java
@OWLClass(iri = "http://onto.fel.cvut.cz/ontologies/ufo/Person")
public class User {

    @Id
    public URI uri;

    @OWLDataProperty(iri = "http://xmlns.com/foaf/0.1/firstName")
    private String firstName;

    @OWLDataProperty(iri = "http://xmlns.com/foaf/0.1/lastName")
    private String lastName;
    
    @OWLDataProperty(iri = "http://xmlns.com/foaf/0.1/accountName")
    private String username;

    @Properties
    private Map<String, Set<String>> properties;
    
    // Getters and setters follow
}
```

### JSON-LD

```JSON
{
  "@context": {
    "firstName": "http://xmlns.com/foaf/0.1/firstName",
    "lastName": "http://xmlns.com/foaf/0.1/lastName",
    "accountName": "http://xmlns.com/foaf/0.1/accountName",
    "isAdmin": "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/isAdmin"
  },
  "@id": "http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Catherine+Halsey",
  "@type": [
    "http://onto.fel.cvut.cz/ontologies/ufo/Person",
    "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/User",
    "http://onto.fel.cvut.cz/ontologies/ufo/Agent"
  ],
  "isAdmin": true,
  "accountName": "halsey@unsc.org",
  "firstName": "Catherine",
  "lastName": "Halsey"
}
```

## Configuration

Parameter | Default value | Explanation
----------|---------------|-----------
`ignoreUnknownProperties` | `false` | Whether to ignore unknown properties when deserializing JSON-LD. Default behavior throws an exception.

See `cz.cvut.kbss.jsonld.ConfigParam`.

## Getting JB4JSON-LD

There are two ways to get JB4JSON-LD:

* Clone repository/download zip and build it with Maven,
* Use a [Maven dependency](http://search.maven.org/#search%7Cga%7C1%7Ccz.cvut.kbss.jsonld):

```XML
<dependency>
    <groupId>cz.cvut.kbss.jsonld</groupId>
    <artifactId>jb4jsonld</artifactId>
</dependency>
```

Note that you will most likely need an integration with a JSON-serialization library like [JB4JSON-LD-Jackson](https://github.com/kbss-cvut/jb4jsonld-jackson).
