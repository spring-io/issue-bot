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

import org.junit.Test;

import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.User;
import io.spring.issuebot.support.MultiValueMaps;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link OpenedByCollaboratorTriageFilter}.
 *
 * @author Andy Wilkinson
 */
public class OpenedByCollaboratorTriageFilterTests {

	private final String repositoryUrl = "https://api.github.com/repos/testorg/testrepo";

	private TriageFilter filter = new OpenedByCollaboratorTriageFilter(
			MultiValueMaps.from("testorg/testrepo", "Adam", "Brenda", "Charlie"));

	@Test
	public void openedByCollaborator() {
		assertThat(this.filter.triaged(
				new Issue(null, null, null, null, this.repositoryUrl, new User("Adam"), null, null, null)),
				is(true));
	}

	@Test
	public void openedByAnotherUser() {
		assertThat(this.filter.triaged(
				new Issue(null, null, null, null, this.repositoryUrl, new User("Debbie"), null, null, null)),
				is(false));
	}

}
