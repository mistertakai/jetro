/*
 * #%L
 * Jetro JsonPath
 * %%
 * Copyright (C) 2013 - 2020 The original author or authors.
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
package net.sf.jetro.path;

class MatchesAllFurtherPathElement extends JsonPathElement {
	private static final long serialVersionUID = 3355362025443127736L;

	MatchesAllFurtherPathElement() {
		super(false, false);
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.jetro.path.JsonPathElement#toString()
	 */
	@Override
	public String toString() {
		return ":";
	}
}