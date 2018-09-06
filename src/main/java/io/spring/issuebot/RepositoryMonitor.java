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

package io.spring.issuebot;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;

/**
 * Central class for monitoring the configured repositories.
 *
 * @author Andy Wilkinson
 */
class RepositoryMonitor {

	private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

	private final GitHubOperations gitHub;

	private final List<MonitoredRepository> repositories;

	private final List<IssueListener> issueListeners;

	RepositoryMonitor(GitHubOperations gitHub, MonitoredRepository repository,
			List<IssueListener> issueListeners) {
		this.gitHub = gitHub;
		this.repositories = Arrays.asList(repository);
		this.issueListeners = issueListeners;
	}

	RepositoryMonitor(GitHubOperations gitHub, List<MonitoredRepository> repositories,
			List<IssueListener> issueListeners) {
		this.gitHub = gitHub;
		this.repositories = repositories;
		this.issueListeners = issueListeners;
	}

	//TODO: make configurable
	@Scheduled(fixedRate = 5 * 60 * 1000)
	void monitor() {
		for (MonitoredRepository repository : this.repositories) {
			log.info("Monitoring {}/{}", repository.getOrganization(),
					repository.getName());
			try {
				Page<Issue> page = this.gitHub.getIssues(repository.getOrganization(),
						repository.getName());
				while (page != null) {
					for (Issue issue : page.getContent()) {
						for (IssueListener issueListener : this.issueListeners) {
							try {
								issueListener.onOpenIssue(issue);
							}
							catch (Exception ex) {
								log.warn("Listener '{}' failed when handling issue '{}'",
										issueListener, issue, ex);
							}
						}
					}
					page = page.next();
				}
			}
			catch (Exception ex) {
				log.warn("A failure occurred during issue monitoring", ex);
			}
			log.info("Monitoring of {}/{} completed", repository.getOrganization(),
					repository.getName());
		}
	}

}
