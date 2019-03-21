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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TriageFilter} that considers an issue as having been triaged if a milestone
 * has been applied to it.
 *
 * @author Andy Wilkinson
 */
final class MilestoneAppliedTriageFilter implements TriageFilter {

	private static final Logger log = LoggerFactory.getLogger(LabelledTriageFilter.class);

	@Override
	public boolean triaged(Repository repository, Issue issue) {
		if (issue.getMilestone() != null) {
			log.debug("Issue has been triaged. It has been added to milestone {}",
					issue.getMilestone().getTitle());
			return true;
		}
		return false;
	}

}
