# JAXB - JSON-LD

JAXB-JSON-LD is a simple library for serialization of Java objects into JSON-LD and vice versa.

Note that this is the core, abstract implementation. For actual usage, a binding like [https://github.com/kbss-cvut/jaxb-jsonld-jackson](https://github.com/kbss-cvut/jaxb-jsonld-jackson)
has to be used.

## Usage

JAXB-JSON-LD is based on annotations from [JOPA](https://github.com/kbss-cvut/jopa), which enable POJO attributes
to be mapped to ontological constructs (i.e. to object, data or annotation properties) and Java classes to ontological
classes.

Use `@OWLDataProperty` to annotate data fields and `@OWLObjectProperty` to annotate fields referencing other mapped entities.
