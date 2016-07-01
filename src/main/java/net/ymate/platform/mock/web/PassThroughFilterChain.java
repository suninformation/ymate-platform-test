package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.*;
import java.io.IOException;

public class PassThroughFilterChain implements FilterChain {

    private Filter filter;

    private FilterChain nextFilterChain;

    private Servlet servlet;

    public PassThroughFilterChain(Filter filter, FilterChain nextFilterChain) {
        Assert.assertNotNull("Filter must not be null", filter);
        Assert.assertNotNull("'FilterChain must not be null", nextFilterChain);
        this.filter = filter;
        this.nextFilterChain = nextFilterChain;
    }

    public PassThroughFilterChain(Servlet servlet) {
        Assert.assertNotNull("Servlet must not be null", servlet);
        this.servlet = servlet;
    }

    public void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (this.filter != null) {
            this.filter.doFilter(request, response, this.nextFilterChain);
        } else {
            this.servlet.service(request, response);
        }
    }

}
