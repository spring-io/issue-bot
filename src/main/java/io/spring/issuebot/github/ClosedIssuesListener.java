package io.spring.issuebot.github;

import io.spring.issuebot.IssueMonitor;
import io.spring.issuebot.Repository;
import io.spring.issuebot.RepositoryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClosedIssuesListener implements RepositoryListener {

	private static final Logger log = LoggerFactory.getLogger(ClosedIssuesListener.class);

	private final GitHubOperations gitHub;

	private final IssueMonitor issueMonitor;

	public ClosedIssuesListener(GitHubOperations gitHub, IssueMonitor issueMonitor) {
		this.issueMonitor = issueMonitor;
		this.gitHub = gitHub;
	}

	@Override
	public void handle(Repository repository) {
		try {
			Page<Issue> page = this.gitHub.getClosedIssuesWithLabel(
					repository.getOrganization(), repository.getName(),
					"status: waiting-for-triage");
			issueMonitor.monitorIssue(page, (issue, issueListener) -> issueListener
					.onIssueClosure(repository, issue));
		}
		catch (Exception ex) {
			log.warn("A failure occurred during monitoring of {}/{}",
					repository.getOrganization(), repository.getName(), ex);
		}
	}

}
