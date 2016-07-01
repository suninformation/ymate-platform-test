package net.ymate.platform.mock.web;

import org.junit.Assert;

import java.lang.reflect.Array;
import java.util.*;

class HeaderValueHolder {

    private final List<Object> values = new LinkedList<Object>();


    public void setValue(Object value) {
        this.values.clear();
        this.values.add(value);
    }

    public void addValue(Object value) {
        this.values.add(value);
    }

    public void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    public void addValueArray(Object values) {
        Object[] _targetArr = null;
        if (values instanceof Object[]) {
            _targetArr = (Object[]) values;
        } else if (values == null) {
            _targetArr = new Object[0];
        } else if (!values.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + values);
        } else {
            int length = Array.getLength(values);
            if (length == 0) {
                _targetArr = new Object[0];
            } else {
                Class wrapperType = Array.get(values, 0).getClass();
                Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
                for (int i = 0; i < length; ++i) {
                    newArray[i] = Array.get(values, i);
                }
                _targetArr = newArray;
            }
        }
        this.values.addAll(Arrays.asList(_targetArr));
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<String>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    public String getStringValue() {
        return (!this.values.isEmpty() ? this.values.get(0).toString() : null);
    }

    public static HeaderValueHolder getByName(Map<String, HeaderValueHolder> headers, String name) {
        Assert.assertNotNull("Header name must not be null", name);
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return headers.get(headerName);
            }
        }
        return null;
    }

}
