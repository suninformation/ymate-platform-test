package net.ymate.platform.mock.web;

import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import javax.activation.FileTypeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MockServletContext implements ServletContext {

    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private final Log logger = LogFactory.getLog(getClass());

    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private final Set<String> declaredRoles = new HashSet<String>();

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();

    private final ClassLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private int majorVersion = 2;

    private int minorVersion = 5;

    private int effectiveMajorVersion = 2;

    private int effectiveMinorVersion = 5;

    private String servletContextName = "MockServletContext";

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

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

        // Use JVM temp dir as ServletContext temp dir.
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

    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    public String getMimeType(String filePath) {
        String mimeType = MimeTypeResolver.getMimeType(filePath);
        return ("application/octet-stream".equals(mimeType)) ? null : mimeType;
    }

    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        URL resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        if (resource == null) {
            return Collections.emptySet();
        }
        try {
            File file = new File(resource.getFile());
            String[] fileList = file.list();
            if (ArrayUtils.isEmpty(fileList)) {
                return Collections.emptySet();
            }
            Set<String> resourcePaths = new LinkedHashSet<String>(fileList.length);
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

    public URL getResource(String path) throws MalformedURLException {
        return this.resourceLoader.getResource(getResourceLocation(path));
    }

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

    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new MockRequestDispatcher(path);
    }

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
        if (StringUtils.isBlank(defaultServletName)) {
            throw new AssertionError("defaultServletName must not be null or empty");
        }
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    public Servlet getServlet(String name) {
        return null;
    }

    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(new HashSet<Servlet>());
    }

    public Enumeration<String> getServletNames() {
        return Collections.enumeration(new HashSet<String>());
    }

    public void log(String message) {
        logger.info(message);
    }

    public void log(Exception ex, String message) {
        logger.info(message, ex);
    }

    public void log(String message, Throwable ex) {
        logger.info(message, ex);
    }

    public String getRealPath(String path) {
        URL resource = this.resourceLoader.getResource(getResourceLocation(path));
        File _targetFile = FileUtils.toFile(resource);
        if (_targetFile == null) {
            return null;
        }
        return _targetFile.getAbsolutePath();
    }

    public String getServerInfo() {
        return "MockServletContext";
    }

    public String getInitParameter(String name) {
        Assert.assertNotNull("Parameter name must not be null", name);
        return this.initParameters.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

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

    public Object getAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        return this.attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    public void setAttribute(String name, Object value) {
        Assert.assertNotNull("Attribute name must not be null", name);
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    public void removeAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    public String getServletContextName() {
        return this.servletContextName;
    }

    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    public void declareRoles(String... roleNames) {
        Assert.assertNotNull("Role names array must not be null", roleNames);
        for (String roleName : roleNames) {
            if (StringUtils.isBlank(roleName)) {
                throw new AssertionError("Role name must not be empty");
            }
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    private static class MimeTypeResolver {

        public static String getMimeType(String filePath) {
            return FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        }
    }

}
