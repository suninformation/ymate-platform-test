package net.ymate.platform.mock.web;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

public class MockBodyContent extends BodyContent {

    private final String content;

    public MockBodyContent(String content, HttpServletResponse response) {
        this(content, response, null);
    }

    public MockBodyContent(String content, Writer targetWriter) {
        this(content, null, targetWriter);
    }

    public MockBodyContent(String content, HttpServletResponse response, Writer targetWriter) {
        super(adaptJspWriter(targetWriter, response));
        this.content = content;
    }

    private static JspWriter adaptJspWriter(Writer targetWriter, HttpServletResponse response) {
        if (targetWriter instanceof JspWriter) {
            return (JspWriter) targetWriter;
        } else {
            return new MockJspWriter(response, targetWriter);
        }
    }

    public Reader getReader() {
        return new StringReader(this.content);
    }

    public String getString() {
        return this.content;
    }

    public void writeOut(Writer writer) throws IOException {
        writer.write(this.content);
    }

    public void clear() throws IOException {
        getEnclosingWriter().clear();
    }

    public void clearBuffer() throws IOException {
        getEnclosingWriter().clearBuffer();
    }

    public void close() throws IOException {
        getEnclosingWriter().close();
    }

    public int getRemaining() {
        return getEnclosingWriter().getRemaining();
    }

    public void newLine() throws IOException {
        getEnclosingWriter().println();
    }

    public void write(char value[], int offset, int length) throws IOException {
        getEnclosingWriter().write(value, offset, length);
    }

    public void print(boolean value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(char value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(char[] value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(double value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(float value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(int value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(long value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(Object value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void print(String value) throws IOException {
        getEnclosingWriter().print(value);
    }

    public void println() throws IOException {
        getEnclosingWriter().println();
    }

    public void println(boolean value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(char value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(char[] value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(double value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(float value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(int value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(long value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(Object value) throws IOException {
        getEnclosingWriter().println(value);
    }

    public void println(String value) throws IOException {
        getEnclosingWriter().println(value);
    }

}
