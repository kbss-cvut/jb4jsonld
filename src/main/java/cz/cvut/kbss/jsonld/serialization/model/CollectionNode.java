package cz.cvut.kbss.jsonld.serialization.model;

public abstract class CollectionNode extends CompositeNode {

    CollectionNode() {
    }

    CollectionNode(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + items + "}";
    }
}
