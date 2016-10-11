# JAXB - JSON-LD

JAXB-JSON-LD is a simple library for serialization of Java objects into JSON-LD and vice versa.

Note that this is the core, abstract implementation. For actual usage, a binding like 
[https://github.com/kbss-cvut/jaxb-jsonld-jackson](https://github.com/kbss-cvut/jaxb-jsonld-jackson)
has to be used.

More information can be found at [https://kbss.felk.cvut.cz/web/portal/jaxb-jsonld](https://kbss.felk.cvut.cz/web/portal/jaxb-jsonld).

## Usage

JAXB-JSON-LD is based on annotations from [JOPA](https://github.com/kbss-cvut/jopa), which enable POJO attributes
to be mapped to ontological constructs (i.e. to object, data or annotation properties) and Java classes to ontological
classes.

Use `@OWLDataProperty` to annotate data fields and `@OWLObjectProperty` to annotate fields referencing other mapped entities.

See [https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld](https://github.com/kbss-cvut/jopa-examples/tree/master/jsonld) for
an executable example of JAXB JSON-LD in action (together with Spring and Jackson).

## Getting JAXB-JSON-LD

There are two ways to get JAXB-JSON-LD:

* Clone repository/download zip and build it with maven
* Use a Maven dependency from our maven repo at [http://kbss.felk.cvut.cz/m2repo/](http://kbss.felk.cvut.cz/m2repo/)
