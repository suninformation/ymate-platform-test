package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class MockServletConfig implements ServletConfig {

    private final ServletContext servletContext;

    private final String servletName;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    public MockServletConfig() {
        this(null, "");
    }

    public MockServletConfig(String servletName) {
        this(null, servletName);
    }

    public MockServletConfig(ServletContext servletContext) {
        this(servletContext, "");
    }

    public MockServletConfig(ServletContext servletContext, String servletName) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.servletName = servletName;
    }


    public String getServletName() {
        return this.servletName;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void addInitParameter(String name, String value) {
        Assert.assertNotNull("Parameter name must not be null", name);
        this.initParameters.put(name, value);
    }

    public String getInitParameter(String name) {
        Assert.assertNotNull("Parameter name must not be null", name);
        return this.initParameters.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

}
