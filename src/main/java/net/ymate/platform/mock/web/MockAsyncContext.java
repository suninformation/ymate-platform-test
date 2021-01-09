package net.ymate.platform.mock.web;

import org.junit.Assert;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MockAsyncContext implements AsyncContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final List<AsyncListener> listeners = new ArrayList<>();

    private String dispatchedPath;

    private long timeout = 10 * 1000L;

    private final List<Runnable> dispatchHandlers = new ArrayList<>();

    public MockAsyncContext(ServletRequest request, ServletResponse response) {
        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
    }

    public void addDispatchHandler(Runnable handler) {
        Assert.assertNotNull("Dispatch handler must not be null", handler);
        synchronized (this) {
            if (this.dispatchedPath == null) {
                this.dispatchHandlers.add(handler);
            } else {
                handler.run();
            }
        }
    }

    @Override
    public ServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ServletResponse getResponse() {
        return this.response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return (this.request instanceof MockHttpServletRequest && this.response instanceof MockHttpServletResponse);
    }

    @Override
    public void dispatch() {
        dispatch(this.request.getRequestURI());
    }

    @Override
    public void dispatch(String path) {
        dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        synchronized (this) {
            this.dispatchedPath = path;
            for (Runnable r : this.dispatchHandlers) {
                r.run();
            }
        }
    }

    public String getDispatchedPath() {
        return this.dispatchedPath;
    }

    @SuppressWarnings("unchecked")
    private <T> T doGetNativeRequest(ServletRequest request, Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(request)) {
                return (T) request;
            }
            if (request instanceof ServletRequestWrapper) {
                return doGetNativeRequest(((ServletRequestWrapper) request).getRequest(), requiredType);
            }
        }
        return null;
    }

    @Override
    public void complete() {
        MockHttpServletRequest mockRequest = doGetNativeRequest(request, MockHttpServletRequest.class);
        if (mockRequest != null) {
            mockRequest.setAsyncStarted(false);
        }
        for (AsyncListener listener : this.listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException ex) {
                throw new IllegalStateException("AsyncListener failure", ex);
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        this.listeners.add(listener);
    }

    public List<AsyncListener> getListeners() {
        return this.listeners;
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }
}
