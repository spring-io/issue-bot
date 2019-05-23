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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RepositoryMonitor}.
 *
 * @author Andy Wilkinson
 */
public class RepositoryMonitorTests {

	private final RepositoryListener repositoryListenerOne = mock(
			RepositoryListener.class);

	private final RepositoryListener repositoryListenerTwo = mock(
			RepositoryListener.class);

	private final Repository repositoryOne = new Repository();

	private final Repository repositoryTwo = new Repository();

	private final RepositoryMonitor repositoryMonitor = new RepositoryMonitor(
			Arrays.asList(this.repositoryOne, this.repositoryTwo),
			Arrays.asList(this.repositoryListenerOne, this.repositoryListenerTwo));

	@Before
	public void setUp() {
		this.repositoryOne.setOrganization("test");
		this.repositoryOne.setName("one");
		this.repositoryTwo.setOrganization("test");
		this.repositoryTwo.setName("two");
	}

	@Test
	public void noRepositories() {
		RepositoryMonitor monitor = new RepositoryMonitor(Collections.emptyList(),
				Arrays.asList(this.repositoryListenerOne, this.repositoryListenerTwo));
		monitor.monitor();
		verifyNoMoreInteractions(repositoryListenerOne, repositoryListenerTwo);
	}

	@Test
	public void noIssueListeners() {
		RepositoryMonitor monitor = new RepositoryMonitor(
				Arrays.asList(repositoryOne, repositoryTwo), Collections.emptyList());
		monitor.monitor();
		verifyNoMoreInteractions(repositoryListenerTwo, repositoryListenerTwo);
	}

	@Test
	public void monitorDelegatesToListeners() {
		this.repositoryMonitor.monitor();
		verify(repositoryListenerOne).handle(repositoryOne);
		verify(repositoryListenerOne).handle(repositoryTwo);
		verify(repositoryListenerTwo).handle(repositoryOne);
		verify(repositoryListenerTwo).handle(repositoryTwo);
	}

}
