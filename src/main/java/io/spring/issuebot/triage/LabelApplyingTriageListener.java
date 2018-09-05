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

import java.util.Map;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;

/**
 * A {@link TriageListener} that applies a label to any issues that require triage.
 *
 * @author Andy Wilkinson
 */
final class LabelApplyingTriageListener implements TriageListener {

	private final GitHubOperations gitHub;

	private final Map<String, String> label;

	/**
	 * Creates a new {@code LabelApplyingTriageListener} that will use the given
	 * {@code gitHubOperations} to apply the given {@code label} to any issues that
	 * require triage.
	 *
	 * @param gitHubOperations the GitHubOperations
	 * @param label the label
	 */
	LabelApplyingTriageListener(GitHubOperations gitHubOperations, Map<String, String> label) {
		this.gitHub = gitHubOperations;
		this.label = label;
	}

	@Override
	public void requiresTriage(Issue issue) {
		this.gitHub.addLabel(issue, findLabel(issue));
	}

	@Override
	public void doesNotRequireTriage(Issue issue) {
		this.gitHub.removeLabel(issue, findLabel(issue));
	}

	private String findLabel(Issue issue) {
		Issue.Slug slug = issue.slug();
		if (this.label.containsKey(slug.toString())) {
			return this.label.get(slug.toString());
		}
		else if (this.label.containsKey(slug.getRepo())) {
			return this.label.get(slug.getRepo());
		}
		else if (this.label.containsKey(slug.getOrg())) {
			return this.label.get(slug.getOrg());
		}
		return null;
	}

}
