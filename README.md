# Java Binding for JSON-LD

[![Build Status](https://kbss.felk.cvut.cz/jenkins/buildStatus/icon?job=jaxb-jsonld)](https://kbss.felk.cvut.cz/jenkins/job/jaxb-jsonld)

Java Binding for JSON-LD (JB4JSON-LD) is a simple library for serialization of Java objects into JSON-LD and vice versa.

Note that this is the core, abstract implementation. For actual usage, a binding
like [JB4JSON-LD Jackson](https://github.com/kbss-cvut/jb4jsonld-jackson)
has to be used.

## Usage

JB4JSON-LD is based on annotations from [JOPA](https://github.com/kbss-cvut/jopa), which enable POJO attributes to be
mapped to ontological constructs (i.e. to object, data or annotation properties) and Java classes to ontological
classes.

Use `@OWLDataProperty` to annotate data fields and `@OWLObjectProperty` to annotate fields referencing other mapped
entities. You can mark DTOs with `@NonEntity` to tell JOPA to ignore such classes completely (e.g., they will be ignored
when AspectJ is inserting JOPA-specific join points into entity classes).

See [https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld](https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld)
for an executable example of JB4JSON-LD in action (together with Spring and Jackson).
The [JB4JSON-LD Jackson](https://github.com/kbss-cvut/jb4jsonld-jackson)
repository also contains a bare-bones example of setting the library up with Jackson (two DTO classes and a short Main
class).

## Example

### Java

```Java
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

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

    @OWLDataProperty(iri = "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/role")
    private Role role;  // Role is an enum

    @OWLDataProperty(iri = "http://purl.org/dc/terms/created")
    private Instant dateCreated;

    @Properties
    private Map<String, Set<String>> properties;

    // Getters and setters follow
}
```

### JSON-LD

```JSON
{
  "@context": {
    "uri": "@id",
    "types": "@type",
    "firstName": "http://xmlns.com/foaf/0.1/firstName",
    "lastName": "http://xmlns.com/foaf/0.1/lastName",
    "username": "http://xmlns.com/foaf/0.1/accountName",
    "isAdmin": "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/isAdmin",
    "role": "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/role",
    "dateCreated": {
      "@id": "http://purl.org/dc/terms/created",
      "@type": "http://www.w3.org/2001/XMLSchema#dateTime"
    }
  },
  "uri": "http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Catherine+Halsey",
  "types": [
    "http://onto.fel.cvut.cz/ontologies/jb4jsonld/User"
  ],
  "firstName": "Catherine",
  "lastName": "Halsey",
  "username": "halsey@unsc.org",
  "isAdmin": true,
  "role": "USER",
  "dateCreated": "2022-11-24T16:47:44Z"
}
```

## Configuration

| Parameter                                     | Default value | Explanation                                                                                                                                                                                                                                                                  |
|-----------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ignoreUnknownProperties`                     | `false`       | Whether to ignore unknown properties when deserializing JSON-LD. Default behavior throws an exception.                                                                                                                                                                       |
| `scanPackage`                                 | `""`          | Package in which the library should look for mapped classes. The scan is important for support for polymorphism in object deserialization. It is highly recommended to specify this value, otherwise the library will attempt to load and scan all classes on the classpath. |
| `requireId`                                   | `false`       | Whether to require an identifier when serializing an object. If set to `true` and no identifier is found (either there is no `@Id` field or its value is `null`), an exception will be thrown. By default a blank node identifier is generated if no id is present.          |
| `assumeTargetType`                            | `false`       | Whether to allow assuming target type in case the JSON-LD object does not contain types (`@type`). If set to `true`, the provided Java type (deserialization invocation argument, field type) will be used as target type.                                                   |
| `enableOptimisticTargetTypeResolution`        | `false`       | Whether to enable optimistic target type resolution. If enabled, this allows to pick a target type even if there are multiple matching classes (which would normally end with an `AmbiguousTargetTypeException`).                                                            |
| `preferSuperclass`                            | `false`       | Allows to further specify optimistic target type resolution. By default, any of the target classes may be selected. Setting this to `true` will make the resolver attempt to select a superclass of the matching classes (if it is also in the target set).                  |
| `serializeDatetimeAsMillis`                   | `false`       | Whether to serialize datetime values as millis since Unix epoch. If false, datetime value are serialize as string in ISO format (default).                                                                                                                                   |
| `datetimeFormat`                              |               | Format in which datetime values are serialized (and expected for deserialization). Default is undefined, meaning that the ISO 8601 format is used.                                                                                                                           |
| `serializeIndividualsUsingExpandedDefinition` | `false`       | Whether individuals should be serialized as string with expanded term definition in context (consisting of `@id` and `@type`) Relevant only for context-based serializer.                                                                                                    |
| `disableTypeMapCache`                         | `false`       | Disables type map cache. Type map is built for deserialization by scanning the classpath.                                                                                                                                                                                    |
| `disableUnresolvedReferencesCheck`            | `false`       | Whether to disable the unresolved references check after deserialization.                                                                                                                                                                                                    |
See `cz.cvut.kbss.jsonld.ConfigParam`.

## Documentation

Documentation is on the [Wiki](https://github.com/kbss-cvut/jb4jsonld/wiki). API Javadoc is
also [available](https://kbss.felk.cvut.cz/jenkins/view/Java%20Tools/job/jaxb-jsonld/javadoc/).

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

Note that you will most likely need an integration with a JSON-serialization library
like [JB4JSON-LD-Jackson](https://github.com/kbss-cvut/jb4jsonld-jackson).

## License

LGPLv3
