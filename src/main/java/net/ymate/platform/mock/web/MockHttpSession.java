package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

    public static final String SESSION_COOKIE_NAME = "JSESSION";

    private static int nextId = 1;

    private String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

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

    @Override
    public long getCreationTime() {
        assertIsValid();
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String changeSessionId() {
        this.id = Integer.toString(nextId++);
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        assertIsValid();
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        assertIsValid();
        Assert.assertNotNull("Attribute name must not be null", name);
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        assertIsValid();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        assertIsValid();
        return this.attributes.keySet().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        assertIsValid();
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

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        assertIsValid();
        Assert.assertNotNull("Attribute name must not be null", name);
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
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

    @Override
    public void invalidate() {
        assertIsValid();
        this.invalid = true;
        clearAttributes();
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    private void assertIsValid() {
        Assert.assertFalse("The session has already been invalidated", isInvalid());
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    @Override
    public boolean isNew() {
        assertIsValid();
        return this.isNew;
    }

    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            } else {
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    public void deserializeState(Serializable state) {
        Assert.assertTrue("Serialized state needs to be of type [java.util.Map]", state instanceof Map);
        this.attributes.putAll((Map<String, Object>) state);
    }
}
