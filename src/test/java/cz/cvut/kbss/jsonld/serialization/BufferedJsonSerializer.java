package cz.cvut.kbss.jsonld.serialization;

import java.io.IOException;

public class BufferedJsonSerializer implements JsonSerializer {

    private StringBuilder buffer = new StringBuilder();

    private boolean addComa = false;
    private boolean inArray = false;

    @Override
    public void writeFieldName(String name) throws IOException {
        if (addComa) {
            buffer.append(',');
        }
        buffer.append('\"').append(name).append("\":");
    }

    @Override
    public void writeObjectStart() throws IOException {
        if (addComa && inArray) {
            buffer.append(',');
        }
        buffer.append('{');
        this.addComa = false;
    }

    @Override
    public void writeObjectEnd() throws IOException {
        buffer.append('}');
        this.addComa = true;
    }

    @Override
    public void writeArrayStart() throws IOException {
        buffer.append('[');
        this.addComa = false;
        this.inArray = true;
    }

    @Override
    public void writeArrayEnd() throws IOException {
        buffer.append(']');
        this.inArray = false;
        this.addComa = true;
    }

    @Override
    public void writeNumber(Number number) throws IOException {
        if (addComa && inArray) {
            buffer.append(',');
        }
        buffer.append(number);
        this.addComa = true;
    }

    @Override
    public void writeBoolean(boolean value) throws IOException {
        if (addComa && inArray) {
            buffer.append(',');
        }
        buffer.append(value);
        this.addComa = true;
    }

    @Override
    public void writeNull() throws IOException {
        if (addComa && inArray) {
            buffer.append(',');
        }
        buffer.append("null");
        this.addComa = true;
    }

    @Override
    public void writeString(String text) throws IOException {
        if (addComa && inArray) {
            buffer.append(',');
        }
        buffer.append('\"').append(text).append('\"');
        this.addComa = true;
    }

    public String getResult() {
        return buffer.toString();
    }
}
