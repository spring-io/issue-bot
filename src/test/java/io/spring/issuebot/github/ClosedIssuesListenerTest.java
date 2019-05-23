package io.spring.issuebot.github;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.Repository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class ClosedIssuesListenerTest {

	private static String waitingForTriageLabel = "status: waiting-for-triage";

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListenerOne = mock(IssueListener.class);

	private final IssueListener issueListenerTwo = mock(IssueListener.class);

	private final Repository repositoryOne = new Repository();

	private ClosedIssuesListener closedIssuesListener = new ClosedIssuesListener(
			this.gitHub, Arrays.asList(issueListenerOne, issueListenerTwo));

	@Before
	public void setUp() {
		this.repositoryOne.setOrganization("test");
		this.repositoryOne.setName("one");
	}

	@Test
	public void repositoriesWithNoIssues() {
		given(this.gitHub.getClosedIssuesWithLabel("test", "one", waitingForTriageLabel))
				.willReturn(null);
		this.closedIssuesListener.handle(repositoryOne);
		verifyNoMoreInteractions(this.issueListenerOne, this.issueListenerTwo);
	}

	@Test
	public void repositoryWithClosedIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null,
				"closed");
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null,
				"closed");
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getClosedIssuesWithLabel("test", "one", waitingForTriageLabel))
				.willReturn(page);
		this.closedIssuesListener.handle(repositoryOne);
		verify(this.issueListenerOne).onIssueClosure(this.repositoryOne, issueOne);
		verify(this.issueListenerOne).onIssueClosure(this.repositoryOne, issueOne);
	}

	@Test
	public void exceptionFromAnIssueListenerIsHandledGracefully() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null, null, null, null, "closed");
		given(page.getContent()).willReturn(Collections.singletonList(issue));
		given(this.gitHub.getClosedIssuesWithLabel("test", "one", waitingForTriageLabel))
				.willReturn(page);
		willThrow(new RuntimeException()).given(this.issueListenerOne)
				.onIssueClosure(this.repositoryOne, issue);
		this.closedIssuesListener.handle(repositoryOne);
		verify(this.issueListenerOne).onIssueClosure(this.repositoryOne, issue);
		verify(this.issueListenerTwo).onIssueClosure(this.repositoryOne, issue);
	}

	@Test
	public void exceptionFromGitHubIsHandledGracefully() {
		given(this.gitHub.getClosedIssuesWithLabel("test", "one", waitingForTriageLabel))
				.willThrow(new RuntimeException());
		this.closedIssuesListener.handle(repositoryOne);
		verify(this.gitHub).getClosedIssuesWithLabel("test", "one",
				waitingForTriageLabel);
	}

}
