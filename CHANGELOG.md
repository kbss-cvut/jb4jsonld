# JB4JSON-LD Changelog

## 0.15.3 - 2025-07-30
- Improve the consistency of typed literals serialization - use native JSON types when possible (Bug #81).
- Dependency updates: JOPA 2.4.4.

## 0.15.2 - 2025-04-16
- Serialize an empty collection as an empty JSON array when it is passed as the root object for serialization (GH-78).
- Dependency updates: JOPA 2.3.1, test dependencies.

## 0.15.1 - 2024-12-17
- Ensure datatype is output with numeric values to preserve their type on serialization (Enhancement #66).
- Dependency updates: JOPA 2.2.1, build plugins.

## 0.15.0 - 2024-08-26
- Support deserializing objects containing only identifier when `ASSUME_TARGET_TYPE` is enabled (Enhancement #69).
- Cache deserialization type map (Enhancement #68).
- Dependency updates: JOPA 2.0.4, test deps.
- **Breaking change:** Set Java 17 as the minimum Java version.

## 0.14.3 - 2024-04-16
- Fix missing context entry when serializing an empty collection mapped to data/annotation property.

## 0.14.2 - 2024-03-13
- Fix an issue with scanning classpath when running in a Spring Boot 3.2.x bundle (Bug #63).
- Dependency updates: JOPA 1.2.2, test deps.

## 0.14.1 - 2023-11-19
- Bump Jakarta JSON version to 2.1.3.
- Switch to Parsson as Jakarta JSON implementation for tests.

## 0.14.0 - 2023-11-03
- Rewrite API to use Jakarta JSON (**Breaking change**).
- Dependency updates: JOPA 1.1.4, test deps.

## 0.13.1 - 2023-07-31
- Better handle class hierarchies when resolving property access.

## 0.13.0 - 2023-07-31
- Make `BeanAnnotationProcessor.getAncestors` public.
- **Breaking change:** Set Java 11 as minimum Java version.

## 0.12.3 - 2023-04-06
- Support serializing individuals as string with an extended term definition in context (Enhancement #54).
- Fix serialization of types when parent context specifies term mapping.
- Remove unused JSON node creation methods.

## 0.12.2 - 2023-03-30
- Fix an issue with context embedding (Bug #51).

## 0.12.1 - 2023-03-28
- Fix an issue with serialization of a collection of enum constants mapped to individuals.

## 0.12.0 - 2023-03-20
- Implement support for mapping Java enum constants to reference nodes (nodes with `@id`) (Enhancement #48).
- Dependency updates: JOPA 0.21.0, JUnit, Mockito.

## 0.11.0 - 2023-02-08
- Implement support for embedded JSON-LD contexts (Enhancement #43).
  - They allow overriding term mapping inherited from parent context.
- Dependency updates: JOPA 0.20.1. 

## 0.10.2 - 2023-02-01
- Improve consistency of context-based serialization output.
- Fix an issue with serializing multilingual strings marked with `@OWLAnnotationProperty`.
- Dependency updates: JOPA 0.20.0.

## 0.10.1 - 2022-12-21
- Dependency updates: JOPA 0.19.3 (allows marking classes as non-entities).

## 0.10.0 - 2022-11-24
- Add support for serialization with JSON-LD context (`@context`) (Feature #16).
- **BREAKING CHANGE**: Serialize temporal literals with type (date, time, datetime, duration).
- Major serialization code refactoring.
- Major refactoring of tests.
- Dependency updates.

## 0.9.0 - 2022-03-29
- Implement better support for temporal data handling (Feature #10). ISO-based string is now the preferred way of representing temporal values.
- Implement support for custom deserializers (Feature #28).
- Dependency updates: JOPA 0.18.5.

## 0.8.9 - 2022-01-05
- Serialize language-less MultilingualString value with `@none` key (as per JSON-LD 1.1 spec). Support corresponding deserialization as well. (#36)
- Dependency updates.

## 0.8.8 - 2021-07-21
- Deserialization of a plural multilingual attribute now attempts to consolidate the values into as few elements as possible(i.e., if there are three values with language en and two values with language cs, the total number of deserialized elements will be three. Previously, it would have been five, because each value would get its own element in the collection).
- Dependency updates.

## 0.8.7 - 2021-04-10
- Implemented support for custom serializers (Feature #28).

## 0.8.6 - 2021-03-18
- Fixing tests in 0.8.5 (There was an interaction between Mockito and setting context classloader of the current thread in some of the previously executed tests).

## 0.8.5 - 2021-03-18
- Fixed wrong handling of WAR and Spring Boot JAR files (#31)

## 0.8.4 - 2021-01-17
- Fix an issue with deserializing collection elements with `equals`/`hashCode`.
- Support expanding compact IRIs based JOPA namespaces (Feature #14).

## 0.8.3 - 2021-01-01
- Support serializing typed unmapped properties as correct target types (Bug #30).
- Major serialization code refactoring.

## 0.8.2 - 2020-10-30
- Added support for optimistic target type resolution (prevents exceptions when multiple matching target types are found in deserialization) (Feature #26).
- Updated dependencies (JOPA, JSON-LD Java).
- Minor code quality improvements.

## 0.8.1 - 2020-10-15
- Updated JOPA to 0.15.2. This fixes a compatibility issue between JOPA 0.15.0 and 0.15.2.

## 0.8.0 - 2020-09-08
- Add support for language indexing (multilingual strings in objects, mapped to language tagged values in JSON-LD) (Feature #20 ).
- Update dependencies.

## 0.7.1 - 2020-07-19
- Handle deserialization to objects without identifier field.

## 0.7.0 - 2020-07-14
- Implement deferred reference resolution (Feature #17).
- Added support for assuming target type in case a JSON-LD object does not contain types (Feature #21). Has to be enabled via configuration.
- Added support for using Object as deserialization target type (more concrete type is resolved when processing data).

## 0.6.0 - 2020-07-06
- Fix an issue with building the project on JDK 9 or later (Bug #18).
- Relax cardinality constraint checking on deserialization when the filler equals existing value (Issue #24, thanks to @cristianolongo).
- Support type coercion for literals (Bug #22).
- Throw a reasonable exception when invalid JSON-LD is passed in to deserializer (Bug #19).

## 0.5.1 - 2020-03-22
- Add support for (de)serialization of enum values (Feature #15).

## 0.5.0 - 2020-03-02
- Serialize annotation property values which are references to other objects as JSON-LD objects with @id. (Feature #13).
- DTTO for deserialization.
- Update dependencies.

## 0.4.0 - 2019-08-08
- Added support for read-only and write-only property mapping (Feature #12).
- Dependency upgrade - JOPA 0.13.1, jsonld-java 0.12.5.

## 0.3.8 - 2019-05-29
- Dependency updates - JOPA 0.12.2.

## 0.3.7 - 2018-12-19
- Fixed issue with assigning a known instance to a plain identifier field.
- Dependency updates.
- Migration to JUnit 5 and Mockito 2.x.

## 0.3.6 - 2018-09-25
- Handle serialization of instances without type info (throw exception).

## 0.3.5 - 2018-09-07
- Serialize `java.util.Date` as numeric timestamps (Issue #9).

## 0.3.4 - 2018-08-19
- Deserialize objects as IRIs into @Properties map (Bug #8).

## 0.3.3 - 2018-07-09
- Support for automatic generation of blank node identifiers for instances without id value (@Id attribute value is `null` or not present at all) in serialization.
- Allow configuring whether the identifier should be automatically generated or an exception thrown when it is missing.

## 0.3.2 - 2018-05-02
- Allow specifying order in which entity attributes are serialized and deserialized. This is important for object references, because the standard JSON-LD expansion algorithm (used by JB4JSON-LD in deserialization) orders properties in node lexicographically, which is not always suitable.
- Allow an entity to be reconstructed from multiple occurrences throughout the JSON-LD. I.e., when deserialization encounters an object with an already known ID, it will attempt to reopen the instance and add the discovered values to it.
- Automatically generate blank node identifiers for entities without id, so that references to them can be used.

## 0.3.1 - 2018-03-25
- Support for polymorphism in deserialization (both root and attribute level) (Enhancement #7).
- Support for deserializing full JSON-LD objects as plain identifier attributes (Enhancement #5).

## 0.2.1 - 2018-02-12
- Added support for blank node identifiers (Enhancement #6).

## 0.2.0 - 2018-02-05
- Rewrote serialization/deserialization of references to previously visited objects. Now they are serialized as objects with a single attribute - id. (Bug #3)

## 0.1.2 - 2018-02-04
- Support for (de)serialization of plain identifier object properties (Enhancement #4).

## 0.1.1 - 2018-01-11
- Fixed bug #1.