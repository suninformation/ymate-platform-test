package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DelegatingServletOutputStream extends ServletOutputStream {

    private final OutputStream targetStream;

    public DelegatingServletOutputStream(OutputStream targetStream) {
        Assert.assertNotNull("Target OutputStream must not be null", targetStream);
        this.targetStream = targetStream;
    }

    public final OutputStream getTargetStream() {
        return this.targetStream;
    }

    @Override
    public void write(int b) throws IOException {
        this.targetStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.targetStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.targetStream.close();
    }
}
