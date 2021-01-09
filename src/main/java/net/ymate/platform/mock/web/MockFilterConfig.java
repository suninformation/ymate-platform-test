package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class MockFilterConfig implements FilterConfig {

    private final ServletContext servletContext;

    private final String filterName;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    public MockFilterConfig() {
        this(null, "");
    }

    public MockFilterConfig(String filterName) {
        this(null, filterName);
    }

    public MockFilterConfig(ServletContext servletContext) {
        this(servletContext, "");
    }

    public MockFilterConfig(ServletContext servletContext, String filterName) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.filterName = filterName;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void addInitParameter(String name, String value) {
        Assert.assertNotNull("Parameter name must not be null", name);
        this.initParameters.put(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        Assert.assertNotNull("Parameter name must not be null", name);
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }
}
