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

package io.spring.issuebot;

import java.util.Date;
import java.util.List;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;
import io.spring.issuebot.github.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Central class for monitoring the configured repository.
 *
 * @author Andy Wilkinson
 */
class RepositoryMonitor {

	private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

	private final GitHubOperations gitHub;

	private final List<Repository> repositories;

	private final boolean enabled;

	private final List<IssueListener> issueListeners;

	RepositoryMonitor(GitHubOperations gitHub, List<Repository> repositories, boolean enabled,
			List<IssueListener> issueListeners) {
		this.gitHub = gitHub;
		this.repositories = repositories;
		this.enabled = enabled;
		this.issueListeners = issueListeners;
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	void monitor() {
		if (this.enabled) {
			for (Repository repository : this.repositories) {
				monitor(repository);
			}
		}
	}

	private void monitor(Repository repository) {
		log.info("Monitoring {}/{}", repository.getOrganization(), repository.getName());
		try {
			Page<Issue> page = this.gitHub.getIssues(repository.getOrganization(), repository.getName());
			while (page != null) {
				for (Issue issue : page.getContent()) {
					for (IssueListener issueListener : this.issueListeners) {
						try {
							issueListener.onOpenIssue(repository, issue);
						}
						catch (Exception ex) {
							log.warn("Listener '{}' failed when handling issue '{}'", issueListener, issue, ex);
						}
					}
				}
				page = page.next();
			}
		}
		catch (Exception ex) {
			log.warn("A failure occurred during monitoring of {}/{}", repository.getOrganization(),
					repository.getName(), ex);
		}
		RateLimit rateLimit = this.gitHub.getRateLimit();
		if (rateLimit == null) {
			log.info("Monitoring of {}/{} completed. Remaining rate limit unknown", repository.getOrganization(),
					repository.getName());
		}
		else {
			log.info("Monitoring of {}/{} completed. Remaining rate limit {} of {} resets at {}",
					repository.getOrganization(), repository.getName(), rateLimit.getRemaining(), rateLimit.getLimit(),
					new Date(rateLimit.getReset()));
		}

	}

}
