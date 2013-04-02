package net.sf.jetro.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.renderer.DefaultJsonRenderer;

public class JsonArray extends ArrayList<JsonType> implements JsonType {
	private static final long serialVersionUID = -853759861392315220L;

	// JSON path relative to the root element of the JSON tree this element belongs to
	// if null this element is the root element
	private JsonPath path;
	private int pathDepth;

	public JsonArray() {
	}

	public JsonArray(final JsonPath path) {
		this(path, null);
	}

	public JsonArray(final List<? extends JsonType> values) {
		this(null, values);
	}

	public JsonArray(final JsonPath path, final List<? extends JsonType> values) {
		this.path = path;

		if (path != null) {
			pathDepth = path.getDepth();
		}

		if (values != null) {
			this.addAll(values);
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
		builder.append("JsonArray [values=").append(super.toString()).append(", path=").append(path).append("]");
		return builder.toString();
	}

	@Override
	public JsonElement getElementAt(JsonPath path) {
		if (this.path == path || (this.path != null && this.path.equals(path))) {
			return this;
		} else if (pathDepth < path.getDepth() && this.path.isParentPathOf(path) && path.hasArrayIndexAt(pathDepth + 1)) {
			int expectedIndex = path.getArrayIndexAt(pathDepth);

			if (expectedIndex < size()) {
				return get(expectedIndex).getElementAt(path);
			} else {
				throw new NoSuchElementException("No JSON Element could be found at path [" + path + "]");
			}
		} else {
			throw new NoSuchElementException("No JSON Element could be found at path [" + path + "]");
		}
	}
}