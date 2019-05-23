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

public class OpenIssuesListenerTest {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListenerOne = mock(IssueListener.class);

	private final IssueListener issueListenerTwo = mock(IssueListener.class);

	private final Repository repositoryOne = new Repository();

	private OpenIssuesListener openIssuesListener = new OpenIssuesListener(this.gitHub,
			Arrays.asList(issueListenerOne, issueListenerTwo));

	@Before
	public void setUp() {
		this.repositoryOne.setOrganization("test");
		this.repositoryOne.setName("one");
	}

	@Test
	public void repositoriesWithNoIssues() {
		given(this.gitHub.getIssues("test", "one")).willReturn(null);
		this.openIssuesListener.handle(repositoryOne);
		verifyNoMoreInteractions(this.issueListenerOne, this.issueListenerTwo);
	}

	@Test
	public void repositoryWithIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null,
				"open");
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null,
				"open");
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "one")).willReturn(page);
		this.openIssuesListener.handle(repositoryOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueOne);
	}

	@Test
	public void exceptionFromAnIssueListenerIsHandledGracefully() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null, null, null, null, "open");
		given(page.getContent()).willReturn(Collections.singletonList(issue));
		given(this.gitHub.getIssues("test", "one")).willReturn(page);
		willThrow(new RuntimeException()).given(this.issueListenerOne)
				.onOpenIssue(this.repositoryOne, issue);
		this.openIssuesListener.handle(repositoryOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issue);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issue);
	}

	@Test
	public void exceptionFromGitHubIsHandledGracefully() {
		given(this.gitHub.getIssues("test", "one")).willThrow(new RuntimeException());
		this.openIssuesListener.handle(repositoryOne);
		verify(this.gitHub).getIssues("test", "one");
	}

}
