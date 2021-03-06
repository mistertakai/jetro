/*
 * #%L
 * Jetro Patch
 * %%
 * Copyright (C) 2013 - 2019 The original author or authors.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.sf.jetro.patch.pointer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import net.sf.jetro.path.ArrayIndexPathElement;
import net.sf.jetro.path.JsonPath;
import net.sf.jetro.path.PropertyNamePathElement;

public class JsonPointer implements Cloneable, Serializable {
	private static final long serialVersionUID = 2433043640914648968L;

	static final String END_OF_ARRAY = "-";

	private JsonPointerElement<?>[] pointerElements; 
	private int size;
	
	public JsonPointer() {
		this(new JsonPointerElement<?>[] {});
	}
	
	JsonPointer(final JsonPointerElement<?>[] pointerElements) {
		if (pointerElements == null) {
			throw new IllegalArgumentException("pointerElements must not be null");
		}
		
		this.pointerElements = pointerElements;
		this.size = pointerElements.length;
	}

	protected JsonPointer clone() {
		try {
			JsonPointer clone = (JsonPointer) super.clone();

			clone.pointerElements = Arrays.copyOf(pointerElements, size + 1);
			
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	public JsonPath toJsonPath() {
		JsonPath path = new JsonPath();
		
		for (int i = 0; i < size; i++) {
			if (pointerElements[i] instanceof ArrayIndexPointerElement) {
				if (((ArrayIndexPointerElement) pointerElements[i]).isEndOfArray()) {
					path = path.append(new ArrayIndexPathElement());
				} else {
					path = path.append(new ArrayIndexPathElement(
							((ArrayIndexPointerElement) pointerElements[i]).getValue()));
				}
			} else if (pointerElements[i] instanceof PropertyNamePointerElement) {
				path = path.append(new PropertyNamePathElement(
						((PropertyNamePointerElement) pointerElements[i]).getValue()));
			}
		}
		
		return path;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < size; i++) {
			builder.append(pointerElements[i].toString());
		}
		
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JsonPointer other = (JsonPointer) obj;
		return Objects.equals(toString(), other.toString());
	}

	public boolean isRootPath() {
		return size == 0;
	}

	public boolean hasArrayIndexAt(final int depth) {
		return hasArrayIndexElementAt(depth) && !isEndOfArrayAt(depth);
	}
	
	public boolean hasEndOfArrayArrayIndexAt(final int depth) {
		return hasArrayIndexElementAt(depth) && isEndOfArrayAt(depth);
	}
	
	public boolean hasArrayIndexElementAt(final int depth) {
		return pointerElements[depth - 1] instanceof ArrayIndexPointerElement;
	}
	
	private boolean isEndOfArrayAt(final int depth) {
		return ((ArrayIndexPointerElement) pointerElements[depth - 1]).isEndOfArray();
	}
	
	public int getArrayIndexAt(final int depth) {
		if (hasArrayIndexAt(depth)) {
			return ((ArrayIndexPointerElement) pointerElements[depth - 1]).getValue();
		} else {
			throw new IllegalStateException("The pointer element at depth " + depth +
					" in pointer " + this + " is not an array index");
		}
	}

	public boolean hasPropertyNameAt(final int depth) {
		return pointerElements[depth - 1] instanceof PropertyNamePointerElement;
	}
	
	public String getPropertyNameAt(final int depth) {
		if (hasPropertyNameAt(depth)) {
			return ((PropertyNamePointerElement) pointerElements[depth - 1]).getValue();
		} else {
			throw new IllegalStateException("The pointer element at depth " + depth +
					" in pointer " + this + " is not a property name");
		}
	}
	
	public JsonPointer removeLastElement() {
		if (isRootPath()) {
			throw new IllegalStateException("Cannot remove last element from root path.");
		}
		return clone().removeInternal();
	}
	
	private JsonPointer removeInternal() {
		pointerElements[--size] = null;
		return this;
	}
	
	public <T extends Serializable> JsonPointer append(final JsonPointerElement<T> newElement) {
		return clone().appendInternal(newElement);
	}

	private <T extends Serializable> JsonPointer appendInternal(
			final JsonPointerElement<T> newElement) {
		pointerElements[++size - 1] = newElement;
		return this;
	}

	public int getDepth() {
		return size;
	}
	
	public boolean isParentPointerOf(final JsonPointer pointer) {
		boolean parentPointer = true;

		for (int i = 0; i < size; i++) {
			if (!pointerElements[i].equals(pointer.pointerElements[i])) {
				parentPointer = false;
				break;
			}
		}

		return parentPointer;
	}
	
	public static JsonPointer fromJsonPath(final JsonPath path) {
		if (path == null) {
			return null;
		}
		
		if (path.isRootPath()) {
			return new JsonPointer();
		}
		
		if (path.containsOptionals()) {
			throw new JsonPointerException("JsonPaths with optional elements cannot be "
					+ "converted into JsonPointers");
		}
		
		final JsonPointerElement<?>[] pointerElements =
				new JsonPointerElement<?>[path.getDepth()];
		
		for (int i = 0; i < path.getDepth(); i++) {
			if (path.hasWildcardAt(i)) {
				throw new JsonPointerException("JsonPaths with wildcard elements cannot be "
						+ "converted into JsonPointers");
			} else if (path.hasEndOfArrayAt(i)) {
				pointerElements[i] = new ArrayIndexPointerElement();
			} else if (path.hasArrayIndexAt(i)) {
				pointerElements[i] = new ArrayIndexPointerElement(path.getArrayIndexAt(i));
			} else if (path.hasPropertyNameAt(i)) {
				pointerElements[i] = PropertyNamePointerElement.of(path.getPropertyNameAt(i));
			}
		}
		
		return new JsonPointer(pointerElements);
	}

	public static JsonPointer compile(final String jsonPointer) {
		Objects.requireNonNull(jsonPointer);
		
		if (!(jsonPointer.equals("") || jsonPointer.startsWith("/"))) {
			throw new JsonPointerException("jsonPointer is not a valid JsonPointer, "
					+ "since it doesn't start with a separator (/)");
		}
		
		if (jsonPointer.equals("")) {
			return new JsonPointer();
		}
		
		if (jsonPointer.equals("/")) {
			JsonPointerElement<?>[] pointerElements = new JsonPointerElement<?>[1];
			pointerElements[0] = PropertyNamePointerElement.of("");
			return new JsonPointer(pointerElements);
		}
		
		String[] parts = jsonPointer.split("/");
		
		JsonPointerElement<?>[] pointerElements = new JsonPointerElement<?>[parts.length - 1];
		
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].equals(END_OF_ARRAY)) {
				pointerElements[i - 1] = new ArrayIndexPointerElement();
			} else if (isArrayIndex(parts[i])) {
				pointerElements[i - 1] = new ArrayIndexPointerElement(
						Integer.parseInt(parts[i]));
			} else {
				pointerElements[i - 1] = PropertyNamePointerElement.of(parts[i]);
			}
		}
		
		return new JsonPointer(pointerElements);
	}

	private static boolean isArrayIndex(String part) {
		return part.equals("0") || part.matches("[1-9][0-9]*");
	}
}
