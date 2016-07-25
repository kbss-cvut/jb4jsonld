package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Stack;

import static org.junit.Assert.*;

public class JsonLdTreeBuilderTest {

    private JsonLdTreeBuilder treeBuilder = new JsonLdTreeBuilder();

    @Test
    public void openObjectCreatesNewObjectNode() {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
    }

    @Test
    public void openObjectPushesOriginalCurrentToStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
    }

    private Stack<JsonNode> getNodeStack() throws Exception {
        final Field stackField = JsonLdTreeBuilder.class.getDeclaredField("nodeStack");
        stackField.setAccessible(true);
        return (Stack<JsonNode>) stackField.get(treeBuilder);
    }

    @Test
    public void openObjectDoesNotPushOriginalCurrentToStackWhenItIsAlreadyClosed() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        assertTrue(getNodeStack().isEmpty());
        treeBuilder.closeInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    public void closeInstanceClosesNodeAndDoesNothingWhenStackIsEmpty() throws Exception {
        final User u = Generator.generateUser();
        treeBuilder.openInstance(u);
        assertTrue(getNodeStack().isEmpty());
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        treeBuilder.closeInstance(u);
        assertFalse(treeBuilder.getTreeRoot().isOpen());
        assertTrue(getNodeStack().isEmpty());
    }

    @Test
    public void closeObjectPopsOriginalCurrentFromStack() throws Exception {
        final Employee e = Generator.generateEmployee();
        final Organization org = Generator.generateOrganization();
        treeBuilder.openInstance(e);
        treeBuilder.openInstance(org);
        assertTrue(treeBuilder.getTreeRoot() instanceof ObjectNode);
        assertFalse(getNodeStack().isEmpty());
        treeBuilder.closeInstance(org);
        assertTrue(getNodeStack().isEmpty());
        assertNotNull(treeBuilder.getTreeRoot());
    }
}
