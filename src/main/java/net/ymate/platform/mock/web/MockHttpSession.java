package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

    public static final String SESSION_COOKIE_NAME = "JSESSION";


    private static int nextId = 1;

    private final String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private boolean invalid = false;

    private boolean isNew = true;

    public MockHttpSession() {
        this(null);
    }

    public MockHttpSession(ServletContext servletContext) {
        this(servletContext, null);
    }

    public MockHttpSession(ServletContext servletContext, String id) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.id = (id != null ? id : Integer.toString(nextId++));
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    public Object getAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        return this.attributes.get(name);
    }

    public Object getValue(String name) {
        return getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    public String[] getValueNames() {
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    public void setAttribute(String name, Object value) {
        Assert.assertNotNull("Attribute name must not be null", name);
        if (value != null) {
            this.attributes.put(name, value);
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
            }
        } else {
            removeAttribute(name);
        }
    }

    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        Assert.assertNotNull("Attribute name must not be null", name);
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    public void removeValue(String name) {
        removeAttribute(name);
    }

    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    public void invalidate() {
        if (this.invalid) {
            throw new IllegalStateException("The session has already been invalidated");
        }
        this.invalid = true;
        clearAttributes();
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    public boolean isNew() {
        return this.isNew;
    }

    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<String, Serializable>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            } else {
                // Not serializable... Servlet containers usually automatically
                // unbind the attribute in this case.
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    public void deserializeState(Serializable state) {
        if (state instanceof Map) {
            throw new IllegalArgumentException("Serialized state needs to be of type [java.util.Map]");
        }
        this.attributes.putAll((Map<String, Object>) state);
    }

}
