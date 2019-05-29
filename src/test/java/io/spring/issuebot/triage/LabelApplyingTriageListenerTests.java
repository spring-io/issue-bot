/*
 * Copyright 2015-2018 the original author or authors.
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

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link LabelApplyingTriageListener}.
 *
 * @author Andy Wilkinson
 */
public class LabelApplyingTriageListenerTests {

	private GitHubOperations gitHub = mock(GitHubOperations.class);

	private final LabelApplyingTriageListener listener = new LabelApplyingTriageListener(
			this.gitHub, "test");

	@Test
	public void requiresTriage() {
		Issue issue = new Issue(null, null, null, null, null, null, null, null, null);
		this.listener.requiresTriage(issue);
		verify(this.gitHub).addLabel(issue, "test");
	}

	@Test
	public void doesNotRequireTriage() {
		Issue issue = new Issue(null, null, null, null, null, null, null, null, null);
		this.listener.doesNotRequireTriage(issue);
		verify(this.gitHub).removeLabel(issue, "test");
	}

}
