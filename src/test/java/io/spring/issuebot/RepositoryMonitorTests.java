/*
 * Copyright 2015-2025 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link RepositoryMonitor}.
 *
 * @author Andy Wilkinson
 */
class RepositoryMonitorTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListenerOne = mock(IssueListener.class);

	private final IssueListener issueListenerTwo = mock(IssueListener.class);

	private final Repository repositoryOne = new Repository();

	private final Repository repositoryTwo = new Repository();

	private final RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
			Arrays.asList(this.repositoryOne, this.repositoryTwo), true,
			Arrays.asList(this.issueListenerOne, this.issueListenerTwo));

	@BeforeEach
	void setUp() {
		this.repositoryOne.setOrganization("test");
		this.repositoryOne.setName("one");
		this.repositoryTwo.setOrganization("test");
		this.repositoryTwo.setName("two");
	}

	@Test
	void repositoriesWithNoIssues() {
		given(this.gitHub.getIssues("test", "one")).willReturn(null);
		given(this.gitHub.getIssues("test", "two")).willReturn(null);
		this.repositoryMonitor.monitor();
		verifyNoMoreInteractions(this.issueListenerOne, this.issueListenerTwo);
	}

	@Test
	void oneRepositoryWithOpenIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null);
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "one")).willReturn(page);
		given(this.gitHub.getIssues("test", "two")).willReturn(null);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueTwo);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issueOne);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issueTwo);
	}

	@Test
	void bothRepositoriesWithOpenIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null);
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "one")).willReturn(page);
		given(this.gitHub.getIssues("test", "two")).willReturn(page);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issueTwo);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issueOne);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issueTwo);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryTwo, issueOne);
		verify(this.issueListenerOne).onOpenIssue(this.repositoryTwo, issueTwo);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryTwo, issueOne);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryTwo, issueTwo);
	}

	@Test
	void exceptionFromAnIssueListenerIsHandledGracefully() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Collections.singletonList(issue));
		given(this.gitHub.getIssues("test", "one")).willReturn(page);
		willThrow(new RuntimeException()).given(this.issueListenerOne).onOpenIssue(this.repositoryOne, issue);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(this.repositoryOne, issue);
		verify(this.issueListenerTwo).onOpenIssue(this.repositoryOne, issue);
	}

	@Test
	void exceptionFromGitHubIsHandledGracefully() {
		given(this.gitHub.getIssues("test", "one")).willThrow(new RuntimeException());
		this.repositoryMonitor.monitor();
		verify(this.gitHub).getIssues("test", "one");
	}

}
