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

import java.util.List;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.Repository;
import io.spring.issuebot.github.Issue;

/**
 * {@link IssueListener} that identifies open issues that require triage.
 *
 * @author Andy Wilkinson
 */
final class TriageIssueListener implements IssueListener {

	private final List<TriageFilter> triageFilters;

	private final TriageListener triageListener;

	/**
	 * Creates a new {@code TriageIssueListener} that will use the given
	 * {@code triageFilters} to identify issues that require triage and notify the given
	 * {@code triageListener} of those that do.
	 * @param triageFilters the triage filters
	 * @param triageListener the triage listener
	 */
	TriageIssueListener(List<TriageFilter> triageFilters, TriageListener triageListener) {
		this.triageFilters = triageFilters;
		this.triageListener = triageListener;
	}

	@Override
	public void onOpenIssue(Repository repository, Issue issue) {
		if (requiresTriage(repository, issue)) {
			this.triageListener.requiresTriage(issue);
		}
	}

	private boolean requiresTriage(Repository repository, Issue issue) {
		for (TriageFilter filter : this.triageFilters) {
			if (filter.triaged(repository, issue)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onIssueClosure(Repository repository, Issue issue) {
		this.triageListener.doesNotRequireTriage(issue);
	}

}
