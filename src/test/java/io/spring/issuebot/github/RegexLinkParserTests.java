/*
 * Copyright 2015-2018 the original author or authors.
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

package io.spring.issuebot.github;

import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RegexLinkParser}.
 *
 * @author Andy Wilkinson
 */
public class RegexLinkParserTests {

	private final LinkParser linkParser = new RegexLinkParser();

	@Test
	public void emptyInput() {
		assertThat(this.linkParser.parse("")).isEmpty();
	}

	@Test
	public void nullInput() {
		assertThat(this.linkParser.parse(null)).isEmpty();
	}

	@Test
	public void singleLink() {
		Map<String, String> links = this.linkParser.parse("<url>; rel=\"foo\"");
		assertThat(links).hasSize(1);
		assertThat(links).containsEntry("foo", "url");
	}

	@Test
	public void notALink() {
		Map<String, String> links = this.linkParser.parse("<url>; foo bar");
		assertThat(links).isEmpty();
	}

	@Test
	public void multipleLinks() {
		Map<String, String> links = this.linkParser
				.parse("<url-one>; rel=\"foo\", <url-two>; rel=\"bar\"");
		assertThat(links).hasSize(2);
		assertThat(links).containsEntry("foo", "url-one");
		assertThat(links).containsEntry("bar", "url-two");
	}

}
