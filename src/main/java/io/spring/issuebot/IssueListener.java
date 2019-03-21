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

package io.spring.issuebot;

import io.spring.issuebot.github.Issue;

/**
 * An {@code IssueListener} is notified of issues found during monitoring of a specific
 * repository.
 *
 * @author Andy Wilkinson
 */
public interface IssueListener {

	/**
	 * Notification that the given {@code issue} is open.
	 * @param repository the repository to which the issue belongs
	 * @param issue the open issue
	 */
	default void onOpenIssue(Repository repository, Issue issue) {

	}

	/**
	 * Notification that the given {@code issue} is being closed.
	 * @param repository the repository to which the issue belongs
	 * @param issue the open issue
	 */
	default void onIssueClosure(Repository repository, Issue issue) {

	}

}
