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
import java.util.List;

import org.springframework.util.MultiValueMap;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.github.Comment;
import io.spring.issuebot.github.Event;
import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;
import io.spring.issuebot.github.Page;

/**
 * An {@link IssueListener} that processes issues that are waiting for feedback.
 *
 * @author Andy Wilkinson
 */
final class FeedbackIssueListener implements IssueListener {

	private final GitHubOperations gitHub;

	private final FeedbackProperties feedbackProperties;

	private final MultiValueMap<String, String> collaborators;

	private final String username;
	private final FeedbackListener feedbackListener;
	private final boolean includeBotUser;

	FeedbackIssueListener(GitHubOperations gitHub, FeedbackProperties feedbackProperties,
						MultiValueMap<String, String> collaborators, String username,
						FeedbackListener feedbackListener) {
		this.gitHub = gitHub;
		this.feedbackProperties = feedbackProperties;
		this.collaborators = collaborators;
		this.username = username;
		this.feedbackListener = feedbackListener;
		this.includeBotUser = feedbackProperties.isIncludeBotUser();
	}

	@Override
	public void onOpenIssue(Issue issue) {
		if (waitingForFeedback(issue)) {
			OffsetDateTime waitingSince = getWaitingSince(issue);
			if (waitingSince != null) {
				processWaitingIssue(issue, waitingSince);
			}
		}
	}

	private void processWaitingIssue(Issue issue, OffsetDateTime waitingSince) {
		if (commentedSince(waitingSince, issue)) {
			this.feedbackListener.feedbackProvided(issue);
		}
		else {
			this.feedbackListener.feedbackRequired(issue, waitingSince);
		}
	}

	private boolean waitingForFeedback(Issue issue) {
		return issue.getPullRequest() == null && labelledAsWaitingForFeedback(issue);
	}


	private boolean labelledAsWaitingForFeedback(Issue issue) {
		String labelName = findLabel(issue);
		for (Label label : issue.getLabels()) {
			if (labelName.equals(label.getName())) {
				return true;
			}
		}
		return false;
	}

	private OffsetDateTime getWaitingSince(Issue issue) {
		OffsetDateTime createdAt = null;
		String labelName = findLabel(issue);
		Page<Event> page = this.gitHub.getEvents(issue);
		while (page != null) {
			for (Event event : page.getContent()) {
				if (Event.Type.LABELED.equals(event.getType())
						&& labelName.equals(event.getLabel().getName())) {
					createdAt = event.getCreationTime();
				}
			}
			page = page.next();
		}
		return createdAt;
	}

	private String findLabel(Issue issue) {
		Issue.Slug slug = issue.slug();
		FeedbackProperties.Properties properties = slug.find(this.feedbackProperties);
		if (properties != null) {
			return properties.getRequiredLabel();
		}
		return null;
	}

	private boolean commentedSince(OffsetDateTime waitingForFeedbackSince, Issue issue) {
		List<String> collaborators = new ArrayList<>();
		if (this.includeBotUser) {
			collaborators.add(this.username);
		}
		String slug = issue.slug().toString();
		if (this.collaborators.containsKey(slug)) {
			collaborators.addAll(this.collaborators.get(slug));
		}

		Page<Comment> page = this.gitHub.getComments(issue);
		while (page != null) {
			for (Comment comment : page.getContent()) {
				if (!collaborators.contains(comment.getUser().getLogin())
						&& comment.getCreationTime().isAfter(waitingForFeedbackSince)) {
					return true;
				}
			}
			page = page.next();
		}
		return false;
	}

}
