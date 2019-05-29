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

import java.util.List;
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

	private final List<Repository> repositories;

	private final List<RepositoryListener> repositoryListeners;

	RepositoryMonitor(List<Repository> repositories,
			List<RepositoryListener> repositoryListeners) {
		this.repositories = repositories;
		this.repositoryListeners = repositoryListeners;
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	void monitor() {
		for (Repository repository : this.repositories) {
			log.info("Monitoring {}/{}", repository.getOrganization(),
					repository.getName());
			for (RepositoryListener repositoryListener : repositoryListeners) {
				repositoryListener.handle(repository);
			}
			log.info("Monitoring of {}/{} completed", repository.getOrganization(),
					repository.getName());
		}
	}

}
