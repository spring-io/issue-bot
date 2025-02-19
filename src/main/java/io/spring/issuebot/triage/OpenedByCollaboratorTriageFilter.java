/*
 * Copyright 2015-2025 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TriageFilter} that considers an issue as having been triaged if it was opened
 * by a collaborator.
 *
 * @author Andy Wilkinson
 */
final class OpenedByCollaboratorTriageFilter implements TriageFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenedByCollaboratorTriageFilter.class);

	private final Map<Repository, List<String>> repositoryCollaborators;

	OpenedByCollaboratorTriageFilter(List<Repository> repositories) {
		this.repositoryCollaborators = repositories.stream()
			.collect(Collectors.toMap(Function.identity(), Repository::getCollaborators));
	}

	@Override
	public boolean triaged(Repository repository, Issue issue) {
		if (this.repositoryCollaborators.get(repository).contains(issue.getUser().getLogin())) {
			log.debug("{} has been triaged. It was opened by {}", issue, issue.getUser());
			return true;
		}
		return false;
	}

}
