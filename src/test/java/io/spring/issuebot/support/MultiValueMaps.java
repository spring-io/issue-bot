/*
 * Copyright 2015 the original author or authors.
 *
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
 */

package io.spring.issuebot.support;

import java.util.Arrays;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Factory methods for MultiValueMap.
 *
 * @author Spencer Gibb
 */
public final class MultiValueMaps {

	private MultiValueMaps() {
	}

	/**
	 * Factory method for a MultiValueMap.
	 * @param key The key for the entry.
	 * @param values The values for the key.
	 * @return a MultiValueMap with one entry.
	 */
	public static MultiValueMap<String, String> from(String key, String... values) {
		LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put(key, Arrays.asList(values));
		return map;
	}
}
