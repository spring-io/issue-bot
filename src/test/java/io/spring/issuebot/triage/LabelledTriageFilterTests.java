/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;

import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LabelledTriageFilter}.
 *
 * @author Andy Wilkinson
 */
public class LabelledTriageFilterTests {

	private TriageFilter filter = new LabelledTriageFilter();

	private final Repository repository = new Repository();

	@Test
	public void issueWithLabels() {
		assertThat(this.filter.triaged(this.repository, new Issue(null, null, null, null,
				null, Arrays.asList(new Label("test")), null, null))).isTrue();
	}

	@Test
	public void issueWithNullLabels() {
		assertThat(this.filter.triaged(this.repository,
				new Issue(null, null, null, null, null, null, null, null))).isFalse();
	}

	@Test
	public void issueWithNoLabels() {
		assertThat(this.filter.triaged(this.repository, new Issue(null, null, null, null,
				null, Collections.emptyList(), null, null))).isFalse();
	}

}
