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

import java.util.Arrays;
import java.util.Collections;

import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OpenedByCollaboratorTriageFilter}.
 *
 * @author Andy Wilkinson
 */
class OpenedByCollaboratorTriageFilterTests {

	private final Repository repository = new Repository();

	private TriageFilter filter;

	@BeforeEach
	void setUp() {
		this.repository.setCollaborators(Arrays.asList("Alice", "Brenda", "Charlie"));
		this.filter = new OpenedByCollaboratorTriageFilter(Collections.singletonList(this.repository));
	}

	@Test
	void openedByCollaborator() {
		assertThat(this.filter.triaged(this.repository,
				new Issue(null, null, null, null, new User("Alice"), null, null, null)))
			.isTrue();
	}

	@Test
	void openedByAnotherUser() {
		assertThat(this.filter.triaged(this.repository,
				new Issue(null, null, null, null, new User("Debbie"), null, null, null)))
			.isFalse();
	}

}
