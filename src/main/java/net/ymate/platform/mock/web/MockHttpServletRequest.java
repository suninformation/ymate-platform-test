package net.ymate.platform.mock.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MockHttpServletRequest implements HttpServletRequest {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String HOST_HEADER = "Host";

    private static final String CHARSET_PREFIX = "charset=";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM = new DelegatingServletInputStream(IOUtils.toInputStream("", StandardCharsets.UTF_8));

    private static final BufferedReader EMPTY_BUFFERED_READER = new BufferedReader(new StringReader(""));

    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    public static final String DEFAULT_PROTOCOL = "HTTP/1.1";

    public static final String DEFAULT_SCHEME = HTTP;

    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

    public static final String DEFAULT_SERVER_NAME = "localhost";

    public static final int DEFAULT_SERVER_PORT = 80;

    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

    public static final String DEFAULT_REMOTE_HOST = "localhost";

    private final ServletContext servletContext;

    private boolean active = true;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private String characterEncoding;

    private byte[] content;

    private String contentType;

    private final Map<String, String[]> parameters = new LinkedHashMap<>();

    private String protocol = DEFAULT_PROTOCOL;

    private String scheme = DEFAULT_SCHEME;

    private String serverName = DEFAULT_SERVER_NAME;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String remoteAddr = DEFAULT_REMOTE_ADDR;

    private String remoteHost = DEFAULT_REMOTE_HOST;

    private final List<Locale> locales = new LinkedList<>();

    private boolean secure = false;

    private int remotePort = DEFAULT_SERVER_PORT;

    private String localName = DEFAULT_SERVER_NAME;

    private String localAddr = DEFAULT_SERVER_ADDR;

    private int localPort = DEFAULT_SERVER_PORT;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    private MockAsyncContext asyncContext;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    private String authType;

    private Cookie[] cookies;

    private final Map<String, HeaderValueHolder> headers = new LinkedHashMap<>();

    private String method;

    private String pathInfo;

    private String contextPath = "";

    private String queryString;

    private String remoteUser;

    private final Set<String> userRoles = new HashSet<>();

    private Principal userPrincipal;

    private String requestedSessionId;

    private String requestURI;

    private String servletPath = "";

    private HttpSession session;

    private boolean requestedSessionIdValid = true;

    private boolean requestedSessionIdFromCookie = true;

    private boolean requestedSessionIdFromURL = false;

    private final Map<String, Part> parts = new LinkedHashMap<>();

    public MockHttpServletRequest() {
        this(null, "", "");
    }

    public MockHttpServletRequest(String method, String requestURI) {
        this(null, method, requestURI);
    }

    public MockHttpServletRequest(ServletContext servletContext) {
        this(servletContext, "", "");
    }

    public MockHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public boolean isActive() {
        return this.active;
    }

    public void close() {
        this.active = false;
    }

    public void invalidate() {
        close();
        clearAttributes();
    }

    protected void checkActive() throws IllegalStateException {
        if (!this.active) {
            throw new IllegalStateException("Request is not active anymore");
        }
    }

    @Override
    public Object getAttribute(String name) {
        checkActive();
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkActive();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        if (StringUtils.isNotBlank(this.contentType)) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) &&
                    StringUtils.isNotBlank(this.characterEncoding)) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.length : -1);
    }

    public long getContentLengthLong() {
        return getContentLength();
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() {
        if (this.content != null) {
            return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
        }
        return EMPTY_SERVLET_INPUT_STREAM;
    }

    public void setParameter(String name, String value) {
        setParameter(name, new String[]{value});
    }

    public void setParameter(String name, String... values) {
        Assert.assertNotNull("Parameter name must not be null", name);
        this.parameters.put(name, values);
    }

    public void setParameters(Map<String, ?> params) {
        Assert.assertNotNull("Parameter map must not be null", params);
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                setParameter(key, (String) value);
            } else if (value instanceof String[]) {
                setParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    public void addParameter(String name, String value) {
        addParameter(name, new String[]{value});
    }

    public void addParameter(String name, String... values) {
        Assert.assertNotNull("Parameter name must not be null", name);
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    public void addParameters(Map<String, ?> params) {
        Assert.assertNotNull("Parameter map must not be null", params);
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                addParameter(key, (String) value);
            } else if (value instanceof String[]) {
                addParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    public void removeParameter(String name) {
        Assert.assertNotNull("Parameter name must not be null", name);
        this.parameters.remove(name);
    }

    public void removeAllParameters() {
        this.parameters.clear();
    }

    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? this.parameters.get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return (name != null ? this.parameters.get(name) : null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        String rawHostHeader = getHeader(HOST_HEADER);
        String host = rawHostHeader;
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                int indexOfClosingBracket = host.indexOf(']');
                Assert.assertTrue("Invalid Host header: " + rawHostHeader, indexOfClosingBracket > -1);
                host = host.substring(0, indexOfClosingBracket + 1);
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
            return host;
        }
        return this.serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        String rawHostHeader = getHeader(HOST_HEADER);
        String host = rawHostHeader;
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                int indexOfClosingBracket = host.indexOf(']');
                Assert.assertTrue("Invalid Host header: " + rawHostHeader, indexOfClosingBracket > -1);
                idx = host.indexOf(':', indexOfClosingBracket);
            } else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        if (this.content != null) {
            InputStream sourceStream = new ByteArrayInputStream(this.content);
            Reader sourceReader = (this.characterEncoding != null) ?
                    new InputStreamReader(sourceStream, this.characterEncoding) :
                    new InputStreamReader(sourceStream);
            return new BufferedReader(sourceReader);
        } else {
            return EMPTY_BUFFERED_READER;
        }
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkActive();
        Assert.assertNotNull("Attribute name must not be null", name);
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkActive();
        Assert.assertNotNull("Attribute name must not be null", name);
        this.attributes.remove(name);
    }

    public void clearAttributes() {
        this.attributes.clear();
    }

    public void addPreferredLocale(Locale locale) {
        Assert.assertNotNull("Locale must not be null", locale);
        this.locales.add(0, locale);
    }

    public void setPreferredLocales(List<Locale> locales) {
        Assert.assertTrue("Locale list must not be empty", locales.isEmpty());
        this.locales.clear();
        this.locales.addAll(locales);
    }

    @Override
    public Locale getLocale() {
        return this.locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(this.scheme));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new MockRequestDispatcher(path);
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
        this.asyncStarted = true;
        this.asyncContext = new MockAsyncContext(request, response);
        return this.asyncContext;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return this.asyncStarted;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    public void setAsyncContext(MockAsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public String getAuthType() {
        return this.authType;
    }

    public void setCookies(Cookie... cookies) {
        this.cookies = cookies;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    public void addHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name) && !this.headers.containsKey(CONTENT_TYPE_HEADER)) {
            setContentType(value.toString());
        } else {
            doAddHeaderValue(name, value, false);
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Assert.assertNotNull("Header value must not be null", value);
        if (header == null || replace) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            header.addValueArray(value);
        } else {
            header.addValue(value);
        }
    }

    public void removeHeader(String name) {
        Assert.assertNotNull("Header name must not be null", name);
        this.headers.remove(name);
    }

    @Override
    public long getDateHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return parseDateHeader(name, (String) value);
        } else if (value != null) {
            throw new IllegalArgumentException("Value for header '" + name + "' is not a Date, Number, or String: " + value);
        } else {
            return -1L;
        }
    }

    private long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            } catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getStringValue() : null);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<String>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value != null) {
            throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
        } else {
            return -1;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || (this.servletContext instanceof MockServletContext &&
                ((MockServletContext) this.servletContext).getDeclaredRoles().contains(role)));
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        String server = getServerName();
        int port = getServerPort();
        String uri = getRequestURI();

        StringBuffer url = new StringBuffer(scheme).append("://").append(server);
        if (port > 0 && ((HTTP.equalsIgnoreCase(scheme) && port != 80) ||
                (HTTPS.equalsIgnoreCase(scheme) && port != 443))) {
            url.append(':').append(port);
        }
        if (StringUtils.isNotBlank(uri)) {
            url.append(uri);
        }
        return url;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    public void setSession(HttpSession session) {
        this.session = session;
        if (session instanceof MockHttpSession) {
            MockHttpSession mockSession = ((MockHttpSession) session);
            mockSession.access();
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        checkActive();
        if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        if (this.session == null && create) {
            this.session = new MockHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    public String changeSessionId() {
        Assert.assertNotNull("The request does not have a session", this.session);
        if (this.session instanceof MockHttpSession) {
            return ((MockHttpSession) this.session).changeSessionId();
        }
        return this.session.getId();
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        this.userPrincipal = null;
        this.remoteUser = null;
        this.authType = null;
    }

    public void addPart(Part part) {
        parts.put(part.getName(), part);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.parts.get(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return parts.values();
    }
}
