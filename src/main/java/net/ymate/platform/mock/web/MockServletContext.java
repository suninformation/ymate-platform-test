package net.ymate.platform.mock.web;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import javax.activation.FileTypeMap;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MockServletContext implements ServletContext {

    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES = new LinkedHashSet<>(3);

    static {
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.COOKIE);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.URL);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.SSL);
    }

    private final Log logger = LogFactory.getLog(getClass());

    private final ClassLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();

    private int majorVersion = 3;

    private int minorVersion = 0;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 0;

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private String servletContextName = "MockServletContext";

    private final Set<String> declaredRoles = new LinkedHashSet<String>();

    private Set<SessionTrackingMode> sessionTrackingModes;

    private final SessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();

    public MockServletContext() {
        this("", null);
    }

    public MockServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    public MockServletContext(ClassLoader resourceLoader) {
        this("", resourceLoader);
    }

    public MockServletContext(String resourceBasePath, ClassLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : ClassUtils.getDefaultClassLoader());
        this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put("javax.servlet.context.tempdir", new File(tempDir));
        }
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    @Override
    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    @Override
    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    @Override
    public String getMimeType(String filePath) {
        String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        return ("application/octet-stream".equals(mimeType) ? null : mimeType);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        URL resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        if (resource == null) {
            return Collections.emptySet();
        }
        try {
            File file = new File(resource.getFile());
            String[] fileList = file.list();
            if (fileList == null || ArrayUtils.isEmpty(fileList)) {
                return Collections.emptySet();
            }
            Set<String> resourcePaths = new LinkedHashSet<>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (new File(file, resultPath).isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        } catch (Exception ex) {
            logger.warn("Couldn't get resource paths for " + resource, ex);
            return Collections.emptySet();
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return this.resourceLoader.getResource(getResourceLocation(path));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        URL resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (resource == null) {
            return null;
        }
        try {
            return resource.openStream();
        } catch (IOException ex) {
            logger.warn("Couldn't open InputStream for " + resource, ex);
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new MockRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String path) {
        return this.namedRequestDispatchers.get(path);
    }

    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.assertNotNull("RequestDispatcher name must not be null", name);
        Assert.assertNotNull("RequestDispatcher must not be null", requestDispatcher);
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    public void unregisterNamedDispatcher(String name) {
        Assert.assertNotNull("RequestDispatcher name must not be null", name);
        this.namedRequestDispatchers.remove(name);
    }

    public String getDefaultServletName() {
        return this.defaultServletName;
    }

    public void setDefaultServletName(String defaultServletName) {
        Assert.assertTrue("defaultServletName must not be null or empty", StringUtils.isBlank(defaultServletName));
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    @Override
    @Deprecated
    public Servlet getServlet(String name) {
        return null;
    }

    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    @Deprecated
    public void log(Exception ex, String message) {
        logger.info(message, ex);
    }

    @Override
    public void log(String message, Throwable ex) {
        logger.info(message, ex);
    }

    @Override
    public String getRealPath(String path) {
        URL resource = this.resourceLoader.getResource(getResourceLocation(path));
        File targetFile = FileUtils.toFile(resource);
        if (targetFile == null) {
            return null;
        }
        return targetFile.getAbsolutePath();
    }

    @Override
    public String getServerInfo() {
        return "MockServletContext";
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

    @Override
    public boolean setInitParameter(String name, String value) {
        Assert.assertNotNull("Parameter name must not be null", name);
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Assert.assertNotNull("Parameter name must not be null", name);
        this.initParameters.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public void setAttribute(String name, Object value) {
        Assert.assertNotNull("Attribute name must not be null", name);
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        Assert.assertNotNull("Role names array must not be null", roleNames);
        for (String roleName : roleNames) {
            Assert.assertTrue("Role name must not be empty", StringUtils.isBlank(roleName));
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
            throws IllegalStateException, IllegalArgumentException {
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return DEFAULT_SESSION_TRACKING_MODES;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return (this.sessionTrackingModes != null ?
                Collections.unmodifiableSet(this.sessionTrackingModes) : DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }
}
