package io.spring.issuebot;

import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IssueMonitor {

	private static final Logger log = LoggerFactory.getLogger(IssueMonitor.class);

	private final List<IssueListener> issueListeners;

	public IssueMonitor(List<IssueListener> issueListeners) {
		this.issueListeners = issueListeners;
	}

	public void monitorIssue(Page<Issue> page, IssueOperation issueOperation) {
		while (page != null) {
			for (Issue issue : page.getContent()) {
				for (IssueListener issueListener : this.issueListeners) {
					try {
						issueOperation.run(issue, issueListener);
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

	@FunctionalInterface
	public interface IssueOperation {

		void run(Issue issue, IssueListener issueListener);

	}

}
