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

package io.spring.issuebot.github;

/**
 * Operations that can be performed against the GitHub API.
 *
 * @author Andy Wilkinson
 */
public interface GitHubOperations {

	/**
	 * Returns the open issues in the {@code repository} owned by the given
	 * {@code organization}.
	 * @param organization the name of the organization
	 * @param repository the name of the repository
	 * @return the issues
	 */
	Page<Issue> getIssues(String organization, String repository);

	/**
	 * Returns the comments that have been made on the given {@code issue}.
	 * @param issue the issue
	 * @return the comments
	 */
	Page<Comment> getComments(Issue issue);

	/**
	 * Adds the given {@code label} to the given {@code issue}.
	 * @param issue the issue
	 * @param label the label
	 * @return the modified issue
	 */
	Issue addLabel(Issue issue, String label);

	/**
	 * Removes the given {@code label} from the given {@code issue}.
	 * @param issue the issue
	 * @param label the label
	 * @return the modified issue
	 */
	Issue removeLabel(Issue issue, String label);

	/**
	 * Adds the given {@code comment} to the given {@code issue}.
	 * @param issue the issue
	 * @param comment the comment
	 * @return the added comment
	 */
	Comment addComment(Issue issue, String comment);

	/**
	 * Closes the given {@code issue}.
	 * @param issue the issue
	 * @return the modified issue
	 */
	Issue close(Issue issue);

	/**
	 * Returns the events that have occurred on the given {@code issue}.
	 * @param issue the issue
	 * @return the events
	 */
	Page<Event> getEvents(Issue issue);

}
