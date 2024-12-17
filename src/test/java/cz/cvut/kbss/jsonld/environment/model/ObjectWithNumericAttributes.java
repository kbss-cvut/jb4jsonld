package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.DEFAULT_PREFIX + "ObjectWithNumericAttributes")
public class ObjectWithNumericAttributes implements GeneratesRdf {

    @Id
    private URI uri;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "shortValue")
    private Short shortValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "intValue")
    private Integer intValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "longValue")
    private Long longValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "floatValue")
    private Float floatValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "doubleValue")
    private Double doubleValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "bigIntegerValue")
    private BigInteger bigIntegerValue;

    @OWLDataProperty(iri = Vocabulary.DEFAULT_PREFIX + "bigDecimalValue")
    private BigDecimal bigDecimalValue;

    public ObjectWithNumericAttributes() {
    }

    public ObjectWithNumericAttributes(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Short getShortValue() {
        return shortValue;
    }

    public void setShortValue(Short shortValue) {
        this.shortValue = shortValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        final IRI subject = vf.createIRI(uri.toString());
        model.add(subject, RDF.TYPE, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "ObjectWithNumericAttributes"));
        if (shortValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "shortValue"), vf.createLiteral(shortValue));
        }
        if (intValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "intValue"), vf.createLiteral(intValue));
        }
        if (longValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "longValue"), vf.createLiteral(longValue));
        }
        if (floatValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "floatValue"), vf.createLiteral(floatValue));
        }
        if (doubleValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "doubleValue"), vf.createLiteral(doubleValue));
        }
        if (bigIntegerValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "bigIntegerValue"),
                      vf.createLiteral(bigIntegerValue));
        }
        if (bigDecimalValue != null) {
            model.add(subject, vf.createIRI(Vocabulary.DEFAULT_PREFIX + "bigDecimalValue"),
                      vf.createLiteral(bigDecimalValue));
        }
    }
}
