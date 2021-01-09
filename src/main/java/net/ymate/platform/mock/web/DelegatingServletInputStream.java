package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelegatingServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;

    public DelegatingServletInputStream(InputStream sourceStream) {
        Assert.assertNotNull("Source InputStream must not be null", sourceStream);
        this.sourceStream = sourceStream;
    }

    public final InputStream getSourceStream() {
        return this.sourceStream;
    }

    @Override
    public int read() throws IOException {
        return this.sourceStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.sourceStream.close();
    }
}
