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
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link StandardFeedbackListener}.
 *
 * @author Andy Wilkinson
 */
public class StandardFeedbackListenerTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListener = mock(IssueListener.class);

	private static final FeedbackProperties properties = new FeedbackProperties();
	static {
		FeedbackProperties.Properties value = new FeedbackProperties.Properties(
			"feedback-required",
			"feedback-provided",
			"feedback-reminder",
			"Please provide requested feedback",
			"Closing due to lack of feedback");

		properties.put("testrepo", value);
	}

	private final FeedbackListener listener = new StandardFeedbackListener(this.gitHub,
			properties, Arrays.asList(this.issueListener));

	private final String repositoryUrl = "https://api.github.com/repos/testorg/testrepo";

	private final Issue issue = new Issue(null, null, null, null, this.repositoryUrl, null, new ArrayList<>(),
			null, null);

	@Test
	public void feedbackProvided() {
		this.listener.feedbackProvided(this.issue);
		verify(this.gitHub).addLabel(this.issue, "feedback-provided");
		verify(this.gitHub).removeLabel(this.issue, "feedback-required");
		verifyNoMoreInteractions(this.gitHub);
	}

	@Test
	public void feedbackProvidedAfterReminder() {
		this.issue.getLabels().add(new Label("feedback-reminder"));
		this.listener.feedbackProvided(this.issue);
		verify(this.gitHub).addLabel(this.issue, "feedback-provided");
		verify(this.gitHub).removeLabel(this.issue, "feedback-required");
		verify(this.gitHub).removeLabel(this.issue, "feedback-reminder");
	}

	@Test
	public void feedbackRequiredButReminderNotYetDue() {
		this.listener.feedbackRequired(this.issue, OffsetDateTime.now());
		verifyNoMoreInteractions(this.gitHub);
	}

	@Test
	public void feedbackRequiredAndReminderDue() {
		this.listener.feedbackRequired(this.issue, OffsetDateTime.now().minusDays(8));
		verify(this.gitHub).addComment(this.issue, "Please provide requested feedback");
		verify(this.gitHub).addLabel(this.issue, "feedback-reminder");
	}

	@Test
	public void feedbackRequiredReminderDueAndAlreadyCommented() {
		this.issue.getLabels().add(new Label("feedback-reminder"));
		this.listener.feedbackRequired(this.issue, OffsetDateTime.now().minusDays(8));
		verifyNoMoreInteractions(this.gitHub);
	}

	@Test
	public void feedbackRequiredAndOverdue() {
		this.listener.feedbackRequired(this.issue, OffsetDateTime.now().minusDays(15));
		verify(this.gitHub).addComment(this.issue, "Closing due to lack of feedback");
		verify(this.gitHub).close(this.issue);
		verify(this.gitHub).removeLabel(this.issue, "feedback-required");
		verify(this.issueListener).onIssueClosure(this.issue);
	}

}
