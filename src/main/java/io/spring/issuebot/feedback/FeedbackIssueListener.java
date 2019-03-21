/*
 * Copyright 2015-2019 the original author or authors.
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

package io.spring.issuebot.feedback;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.Repository;
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

	private final String labelName;

	private final Map<Repository, List<String>> repositoryCollaborators;

	private final FeedbackListener feedbackListener;

	FeedbackIssueListener(GitHubOperations gitHub, String labelName,
			List<Repository> repositories, String username,
			FeedbackListener feedbackListener) {
		this.gitHub = gitHub;
		this.labelName = labelName;
		this.repositoryCollaborators = repositories.stream()
				.collect(Collectors.toMap(Function.identity(), (repository) -> {
					List<String> collaborators = new ArrayList<>(
							repository.getCollaborators());
					collaborators.add(username);
					return collaborators;
				}));
		this.feedbackListener = feedbackListener;
	}

	@Override
	public void onOpenIssue(Repository repository, Issue issue) {
		if (waitingForFeedback(issue)) {
			OffsetDateTime waitingSince = getWaitingSince(issue);
			if (waitingSince != null) {
				processWaitingIssue(repository, issue, waitingSince);
			}
		}
	}

	private void processWaitingIssue(Repository repository, Issue issue,
			OffsetDateTime waitingSince) {
		if (commentedSince(waitingSince, issue,
				this.repositoryCollaborators.get(repository))) {
			this.feedbackListener.feedbackProvided(repository, issue);
		}
		else {
			this.feedbackListener.feedbackRequired(repository, issue, waitingSince);
		}
	}

	private boolean waitingForFeedback(Issue issue) {
		return issue.getPullRequest() == null && labelledAsWaitingForFeedback(issue);
	}

	private boolean labelledAsWaitingForFeedback(Issue issue) {
		for (Label label : issue.getLabels()) {
			if (this.labelName.equals(label.getName())) {
				return true;
			}
		}
		return false;
	}

	private OffsetDateTime getWaitingSince(Issue issue) {
		OffsetDateTime createdAt = null;
		Page<Event> page = this.gitHub.getEvents(issue);
		while (page != null) {
			for (Event event : page.getContent()) {
				if (Event.Type.LABELED.equals(event.getType())
						&& this.labelName.equals(event.getLabel().getName())) {
					createdAt = event.getCreationTime();
				}
			}
			page = page.next();
		}
		return createdAt;
	}

	private boolean commentedSince(OffsetDateTime waitingForFeedbackSince, Issue issue,
			List<String> collaborators) {
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
