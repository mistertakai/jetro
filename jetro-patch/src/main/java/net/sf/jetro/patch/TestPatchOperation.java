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
package net.sf.jetro.patch;

import java.util.Objects;
import java.util.Optional;

import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonType;

public class TestPatchOperation extends ValuePatchOperation {
	public TestPatchOperation(JsonObject patchDefinition) {
		super(patchDefinition);
	}

	@Override
	public JsonType applyPatch(JsonType source) throws JsonPatchException {
		Objects.requireNonNull(source, "Argument 'source' must not be null");
		
		Optional<JsonType> optional = source.getElementAt(path.toJsonPath());
		
		if (optional.isPresent() && optional.get().equals(value)) {
			return handleTarget(source);
		} else {
			throw new JsonPatchException("Expected value [" + value + "] could not be "
					+ "found on source JSON [" + source + "] at path [" + path + "]");
		}
	}
}
