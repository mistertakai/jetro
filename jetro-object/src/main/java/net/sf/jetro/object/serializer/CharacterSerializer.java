/*
 * #%L
 * Jetro Object
 * %%
 * Copyright (C) 2013 - 2016 The original author or authors.
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
package net.sf.jetro.object.serializer;

import net.sf.jetro.visitor.JsonVisitor;

/**
 * @author Matthias Rothe
 * @since 26.02.14
 */
public class CharacterSerializer implements TypeSerializer<Character> {
	@Override
	public boolean canSerialize(Object toSerialize) {
		return toSerialize instanceof Character;
	}

	@Override
	public void serialize(Character toSerialize, JsonVisitor<?> recipient) {
		recipient.visitValue(toSerialize.toString());
	}
}
