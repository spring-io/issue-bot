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

package io.spring.issuebot.feedback;

import java.time.OffsetDateTime;
import java.util.List;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.feedback.FeedbackProperties.Properties;
import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;

/**
 * Standard implementation of {@link FeedbackListener}.
 *
 * @author Andy Wilkinson
 */
final class StandardFeedbackListener implements FeedbackListener {

	private final GitHubOperations gitHub;

	private final FeedbackProperties properties;

	private final List<IssueListener> issueListeners;

	StandardFeedbackListener(GitHubOperations gitHub, FeedbackProperties properties, List<IssueListener> issueListeners) {
		this.gitHub = gitHub;
		this.properties = properties;
		this.issueListeners = issueListeners;
	}

	@Override
	public void feedbackProvided(Issue issue) {
		Properties issueProperties = findProperties(issue);
		this.gitHub.addLabel(issue, issueProperties.getProvidedLabel());
		this.gitHub.removeLabel(issue, issueProperties.getRequiredLabel());
		if (hasReminderLabel(issue, issueProperties)) {
			this.gitHub.removeLabel(issue, issueProperties.getReminderLabel());
		}
	}

	@Override
	public void feedbackRequired(Issue issue, OffsetDateTime requestTime) {
		OffsetDateTime now = OffsetDateTime.now();
		Properties issueProperties = findProperties(issue);
		if (requestTime.plusDays(14).isBefore(now)) {
			close(issue, issueProperties);
		}
		else if (requestTime.plusDays(7).isBefore(now) && !hasReminderLabel(issue, issueProperties)) {
			remind(issue, issueProperties);
		}
	}

	private void close(Issue issue, Properties issueProperties) {
		this.gitHub.addComment(issue, issueProperties.getCloseComment());
		this.gitHub.close(issue);
		this.gitHub.removeLabel(issue, issueProperties.getRequiredLabel());
		this.gitHub.removeLabel(issue, issueProperties.getReminderLabel());
		this.issueListeners.forEach((listener) -> listener.onIssueClosure(issue));
	}

	private boolean hasReminderLabel(Issue issue, Properties issueProperties) {
		if (issue.getLabels() != null) {
			for (Label label : issue.getLabels()) {
				if (issueProperties.getReminderLabel().equals(label.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	private void remind(Issue issue, Properties issueProperties) {
		this.gitHub.addComment(issue, issueProperties.getReminderComment());
		this.gitHub.addLabel(issue, issueProperties.getReminderLabel());
	}

	private Properties findProperties(Issue issue) {
		Issue.Slug slug = issue.slug();
		if (this.properties.containsKey(slug.toString())) {
			return this.properties.get(slug.toString());
		}
		else if (this.properties.containsKey(slug.getRepo())) {
			return this.properties.get(slug.getRepo());
		}
		else if (this.properties.containsKey(slug.getOrg())) {
			return this.properties.get(slug.getOrg());
		}
		return null;
	}

}
