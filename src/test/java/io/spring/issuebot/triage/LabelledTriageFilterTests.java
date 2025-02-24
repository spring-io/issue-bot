/*
 * Copyright 2015-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.issuebot.triage;

import java.util.Collections;

import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LabelledTriageFilter}.
 *
 * @author Andy Wilkinson
 */
class LabelledTriageFilterTests {

	private final TriageFilter filter = new LabelledTriageFilter();

	private final Repository repository = new Repository();

	@Test
	void issueWithLabels() {
		assertThat(this.filter.triaged(this.repository,
				new Issue(null, null, null, null, null, Collections.singletonList(new Label("test")), null, null)))
			.isTrue();
	}

	@Test
	void issueWithNullLabels() {
		assertThat(this.filter.triaged(this.repository, new Issue(null, null, null, null, null, null, null, null)))
			.isFalse();
	}

	@Test
	void issueWithNoLabels() {
		assertThat(this.filter.triaged(this.repository,
				new Issue(null, null, null, null, null, Collections.emptyList(), null, null)))
			.isFalse();
	}

}
