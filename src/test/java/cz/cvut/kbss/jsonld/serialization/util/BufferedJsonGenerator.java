package cz.cvut.kbss.jsonld.serialization.util;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;
import java.util.Stack;

public class BufferedJsonGenerator implements JsonGenerator {

    private StringBuilder buffer = new StringBuilder();

    private boolean firstElement = true;
    private boolean firstAttribute = true;
    private final Stack<NodeType> nodes = new Stack<>();

    private enum NodeType {
        ARRAY, OBJECT
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        if (!firstAttribute) {
            buffer.append(',');
        }
        firstAttribute = false;
        buffer.append('\"').append(name).append("\":");
    }

    @Override
    public void writeObjectStart() throws IOException {
        if (!nodes.isEmpty() && nodes.peek() == NodeType.ARRAY) {
            if (!firstElement) {
                buffer.append(',');
            }
            this.firstElement = false;
        }
        buffer.append('{');
        nodes.push(NodeType.OBJECT);
        this.firstAttribute = true;
    }

    @Override
    public void writeObjectEnd() throws IOException {
        buffer.append('}');
        nodes.pop();
    }

    @Override
    public void writeArrayStart() throws IOException {
        buffer.append('[');
        this.firstElement = true;
        nodes.push(NodeType.ARRAY);
    }

    @Override
    public void writeArrayEnd() throws IOException {
        buffer.append(']');
        nodes.pop();
    }

    @Override
    public void writeNumber(Number number) throws IOException {
        if (!nodes.isEmpty() && nodes.peek() == NodeType.ARRAY) {
            if (!firstElement) {
                buffer.append(',');
            }
            this.firstElement = false;
        }
        buffer.append(number);
    }

    @Override
    public void writeBoolean(boolean value) throws IOException {
        if (!nodes.isEmpty() && nodes.peek() == NodeType.ARRAY) {
            if (!firstElement) {
                buffer.append(',');
            }
            this.firstElement = false;
        }
        buffer.append(value);
    }

    @Override
    public void writeNull() throws IOException {
        if (!nodes.isEmpty() && nodes.peek() == NodeType.ARRAY) {
            if (!firstElement) {
                buffer.append(',');
            }
            this.firstElement = false;
        }
        buffer.append("null");
    }

    @Override
    public void writeString(String text) throws IOException {
        if (!nodes.isEmpty() && nodes.peek() == NodeType.ARRAY) {
            if (!firstElement) {
                buffer.append(',');
            }
            this.firstElement = false;
        }
        buffer.append('\"').append(text).append('\"');
    }

    public String getResult() {
        return buffer.toString();
    }
}
