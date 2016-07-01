package net.ymate.platform.mock.web;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class MockJspWriter extends JspWriter {

    private final HttpServletResponse response;

    private PrintWriter targetWriter;

    public MockJspWriter(HttpServletResponse response) {
        this(response, null);
    }

    public MockJspWriter(Writer targetWriter) {
        this(null, targetWriter);
    }

    public MockJspWriter(HttpServletResponse response, Writer targetWriter) {
        super(DEFAULT_BUFFER, true);
        this.response = (response != null ? response : new MockHttpServletResponse());
        if (targetWriter instanceof PrintWriter) {
            this.targetWriter = (PrintWriter) targetWriter;
        } else if (targetWriter != null) {
            this.targetWriter = new PrintWriter(targetWriter);
        }
    }

    protected PrintWriter getTargetWriter() throws IOException {
        if (this.targetWriter == null) {
            this.targetWriter = this.response.getWriter();
        }
        return this.targetWriter;
    }


    public void clear() throws IOException {
        if (this.response.isCommitted()) {
            throw new IOException("Response already committed");
        }
        this.response.resetBuffer();
    }

    public void clearBuffer() throws IOException {
    }

    public void flush() throws IOException {
        this.response.flushBuffer();
    }

    public void close() throws IOException {
        flush();
    }

    public int getRemaining() {
        return Integer.MAX_VALUE;
    }

    public void newLine() throws IOException {
        getTargetWriter().println();
    }

    public void write(char value[], int offset, int length) throws IOException {
        getTargetWriter().write(value, offset, length);
    }

    public void print(boolean value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(char value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(char[] value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(double value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(float value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(int value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(long value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(Object value) throws IOException {
        getTargetWriter().print(value);
    }

    public void print(String value) throws IOException {
        getTargetWriter().print(value);
    }

    public void println() throws IOException {
        getTargetWriter().println();
    }

    public void println(boolean value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(char value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(char[] value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(double value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(float value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(int value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(long value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(Object value) throws IOException {
        getTargetWriter().println(value);
    }

    public void println(String value) throws IOException {
        getTargetWriter().println(value);
    }

}
