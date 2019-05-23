package io.spring.issuebot.github;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.Repository;
import io.spring.issuebot.RepositoryListener;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClosedIssuesListener implements RepositoryListener {

	private static final Logger log = LoggerFactory.getLogger(ClosedIssuesListener.class);

	private final List<IssueListener> issueListeners;

	private final GitHubOperations gitHub;

	public ClosedIssuesListener(GitHubOperations gitHub,
			List<IssueListener> issueListeners) {
		this.issueListeners = issueListeners;
		this.gitHub = gitHub;
	}

	@Override
	public void handle(Repository repository) {
		try {
			Page<Issue> page = this.gitHub.getClosedIssuesWithLabel(
					repository.getOrganization(), repository.getName(),
					"status: waiting-for-triage");
			while (page != null) {
				for (Issue issue : page.getContent()) {
					for (IssueListener issueListener : this.issueListeners) {
						try {
							issueListener.onIssueClosure(repository, issue);
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
			log.warn("A failure occurred during monitoring of {}/{}",
					repository.getOrganization(), repository.getName(), ex);
		}
	}

}
