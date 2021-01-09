package net.ymate.platform.mock.web;

import org.junit.Assert;

import java.util.*;

class HeaderValueHolder {

	private final List<Object> values = new LinkedList<>();

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
		if (values instanceof Object[]) {
			Collections.addAll(this.values, (Object[]) values);
		} else if (!values.getClass().isArray()) {
			throw new IllegalArgumentException("Source is not an array: " + values);
		}
	}

	public List<Object> getValues() {
		return Collections.unmodifiableList(this.values);
	}

	public List<String> getStringValues() {
		List<String> stringList = new ArrayList<>(this.values.size());
		for (Object value : this.values) {
			stringList.add(value.toString());
		}
		return Collections.unmodifiableList(stringList);
	}

	public Object getValue() {
		return (!this.values.isEmpty() ? this.values.get(0) : null);
	}

	public String getStringValue() {
		return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
	}

	@Override
	public String toString() {
		return this.values.toString();
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
