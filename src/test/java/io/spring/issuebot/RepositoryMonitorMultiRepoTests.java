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

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link RepositoryMonitor}.
 *
 * @author Andy Wilkinson
 */
public class RepositoryMonitorMultiRepoTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListenerOne = mock(IssueListener.class);

	private final IssueListener issueListenerTwo = mock(IssueListener.class);

	private final RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
			Arrays.asList(new MonitoredRepository("test", "test"),
					new MonitoredRepository("test2", "test2")),
			Arrays.asList(this.issueListenerOne, this.issueListenerTwo));

	@Rule
	public OutputCapture output = new OutputCapture();

	@Test
	public void repositoryWithNoIssues() {
		given(this.gitHub.getIssues("test", "test")).willReturn(null);
		given(this.gitHub.getIssues("test2", "test2")).willReturn(null);
		this.repositoryMonitor.monitor();
		verifyNoMoreInteractions(this.issueListenerOne, this.issueListenerTwo);
		assertNoFailures();
	}

	@Test
	public void singleRepositoryWithOpenIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null);
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(issueOne);
		verify(this.issueListenerOne).onOpenIssue(issueTwo);
		verify(this.issueListenerTwo).onOpenIssue(issueOne);
		verify(this.issueListenerTwo).onOpenIssue(issueTwo);
		assertNoFailures();
	}

	@Test
	public void multipleRepositoriesWithOpenIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null);
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		given(this.gitHub.getIssues("test2", "test2")).willReturn(page);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne, times(2)).onOpenIssue(issueOne);
		verify(this.issueListenerOne, times(2)).onOpenIssue(issueTwo);
		verify(this.issueListenerTwo, times(2)).onOpenIssue(issueOne);
		verify(this.issueListenerTwo, times(2)).onOpenIssue(issueTwo);
		assertNoFailures();
	}
	@Test
	public void exceptionFromAnIssueListenerIsHandledGracefully() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issue));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		willThrow(new RuntimeException()).given(this.issueListenerOne).onOpenIssue(issue);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(issue);
		verify(this.issueListenerTwo).onOpenIssue(issue);
		this.output.expect(containsString("failed when handling issue"));
	}

	@Test
	public void exceptionFromGitHubIsHandledGracefully() {
		given(this.gitHub.getIssues("test", "test")).willThrow(new RuntimeException());
		this.repositoryMonitor.monitor();
		this.output.expect(containsString("A failure occurred during issue monitoring"));
	}

	private void assertNoFailures() {
		this.output.expect(not(containsString("A failure occurred during issue monitoring")));
	}

}
