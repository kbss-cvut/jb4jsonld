package cz.cvut.kbss.jsonld.environment;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

/**
 * Matches two {@link Model}s if they are isomorphic.
 *
 * That is, they contain the same statements (including some blank node magic).
 */
public class IsIsomorphic extends TypeSafeMatcher<Model> {

    private final Model expected;

    public IsIsomorphic(Model expected) {
        this.expected = Objects.requireNonNull(expected);
    }

    @Override
    protected boolean matchesSafely(Model actual) {
        return actual != null && Models.isomorphic(expected, actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("[", ", ", "]", expected);
    }

    public static IsIsomorphic isIsomorphic(Model expected) {
        return new IsIsomorphic(expected);
    }
}
