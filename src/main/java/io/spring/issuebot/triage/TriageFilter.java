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

package io.spring.issuebot.triage;

import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;

/**
 * A {@code TriageFilter} is used to identify issues which are waiting for triage.
 *
 * @author Andy Wilkinson
 */
interface TriageFilter {

	/**
	 * Returns {@code true} if the given issue has already been triaged, otherwise
	 * {@code false}.
	 * @param repository the repository to which the issue belongs
	 * @param issue the issue
	 * @return {@code true} if the issue has been triaged, {@code false} otherwise
	 */
	boolean triaged(Repository repository, Issue issue);

}
