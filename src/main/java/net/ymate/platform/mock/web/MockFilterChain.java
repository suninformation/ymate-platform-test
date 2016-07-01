package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.*;
import java.io.IOException;
import java.util.*;

public class MockFilterChain implements FilterChain {

    private ServletRequest request;

    private ServletResponse response;

    private final List<Filter> filters;

    private Iterator<Filter> iterator;

    public MockFilterChain() {
        this.filters = Collections.emptyList();
    }

    public MockFilterChain(Servlet servlet) {
        this.filters = initFilterList(servlet);
    }

    public MockFilterChain(Servlet servlet, Filter... filters) {
        Assert.assertNotNull("filters cannot be null", filters);
        if (filters.length == 0) {
            throw new AssertionError("filters cannot contain null values");
        }
        this.filters = initFilterList(servlet, filters);
    }

    private static List<Filter> initFilterList(Servlet servlet, Filter... filters) {
        List<Filter> _allFilters = new ArrayList<Filter>();
        if (filters != null && filters.length > 0) {
            _allFilters.addAll(Arrays.asList(filters));
        }
        _allFilters.add(new ServletFilterProxy(servlet));
        return _allFilters;
    }

    public ServletRequest getRequest() {
        return this.request;
    }

    public ServletResponse getResponse() {
        return this.response;
    }

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Assert.assertNotNull("Request must not be null", request);
        Assert.assertNotNull("Response must not be null", response);

        if (this.request != null) {
            throw new IllegalStateException("This FilterChain has already been called!");
        }

        if (this.iterator == null) {
            this.iterator = this.filters.iterator();
        }

        if (this.iterator.hasNext()) {
            Filter nextFilter = this.iterator.next();
            nextFilter.doFilter(request, response, this);
        }

        this.request = request;
        this.response = response;
    }

    public void reset() {
        this.request = null;
        this.response = null;
        this.iterator = null;
    }

    private static class ServletFilterProxy implements Filter {

        private final Servlet delegateServlet;

        private ServletFilterProxy(Servlet servlet) {
            Assert.assertNotNull("servlet cannot be null", servlet);
            this.delegateServlet = servlet;
        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            this.delegateServlet.service(request, response);
        }

        public void init(FilterConfig filterConfig) throws ServletException {
        }

        public void destroy() {
        }

        @Override
        public String toString() {
            return this.delegateServlet.toString();
        }
    }

}
