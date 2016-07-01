package net.ymate.platform.mock.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class MockRequestDispatcher implements RequestDispatcher {

    private final Log logger = LogFactory.getLog(getClass());

    private final String resource;

    public MockRequestDispatcher(String resource) {
        Assert.assertNotNull("resource must not be null", resource);
        this.resource = resource;
    }


    public void forward(ServletRequest request, ServletResponse response) {
        Assert.assertNotNull("Request must not be null", request);
        Assert.assertNotNull("Response must not be null", response);
        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot perform forward - response is already committed");
        }
        getMockHttpServletResponse(response).setForwardedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("MockRequestDispatcher: forwarding to [" + this.resource + "]");
        }
    }

    public void include(ServletRequest request, ServletResponse response) {
        Assert.assertNotNull("Request must not be null", request);
        Assert.assertNotNull("Response must not be null", response);
        getMockHttpServletResponse(response).addIncludedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("MockRequestDispatcher: including [" + this.resource + "]");
        }
    }

    protected MockHttpServletResponse getMockHttpServletResponse(ServletResponse response) {
        if (response instanceof MockHttpServletResponse) {
            return (MockHttpServletResponse) response;
        }
        if (response instanceof HttpServletResponseWrapper) {
            return getMockHttpServletResponse(((HttpServletResponseWrapper) response).getResponse());
        }
        throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
    }

}
