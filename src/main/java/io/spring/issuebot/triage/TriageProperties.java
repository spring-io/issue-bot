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

package io.spring.issuebot.triage;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * {@link EnableConfigurationProperties Configuration properties} for triaging GitHub
 * issues.
 *
 * @author Andy Wilkinson
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "issuebot.triage")
class TriageProperties {

	/**
	 * The name of the label that should be applied to issues that are waiting for triage.
	 */
	private Map<String, String> label = new HashMap<>();

}
