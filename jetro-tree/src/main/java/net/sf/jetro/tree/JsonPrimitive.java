package net.sf.jetro.tree;

import java.util.NoSuchElementException;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.renderer.DefaultJsonRenderer;

public abstract class JsonPrimitive<T> implements JsonType {
	private static final long serialVersionUID = -200661848423590056L;

	// JSON path relative to the root element of the JSON tree this element belongs to
	// if null this element is the root element
	private JsonPath path;
	private T value;

	public JsonPrimitive() {
	}

	public JsonPrimitive(final JsonPath path) {
		this(path, null);
	}

	public JsonPrimitive(final T value) {
		this(null, value);
	}

	public JsonPrimitive(final JsonPath path, final T value) {
		this.path = path;
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(final T value) {
		this.value = value;
	}

	@Override
	public JsonElement getElementAt(JsonPath path) {
		if (this.path == path || (this.path != null && this.path.equals(path))) {
			return this;
		} else {
			throw new NoSuchElementException("No JSON Element could be found at path [" + path + "]");
		}
	}

	@Override
	public String toJson() {
		return new DefaultJsonRenderer().render(this);
	}

	@Override
	public String toJson(final JsonRenderer renderer) {
		return renderer.render(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [value=").append(value).append(", path=").append(path)
			.append("]");
		return builder.toString();
	}
}