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

package io.spring.issuebot.github;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import io.spring.issuebot.github.Issue.ClosureReason;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GitHubTemplate}.
 *
 * @author Andy Wilkinson
 */
class GitHubTemplateTests {

	private final GitHubTemplate gitHub = new GitHubTemplate("username", "password", new RegexLinkParser());

	private final MockRestServiceServer server = MockRestServiceServer
		.createServer((RestTemplate) this.gitHub.getRestOperations());

	@Test
	void noIssues() {
		this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		Page<Issue> issues = this.gitHub.getIssues("org", "repo");
		assertThat(issues.getContent()).isEmpty();
		assertThat(issues.next()).isNull();
	}

	@Test
	void singlePageOfIssues() {
		this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("issues-page-one.json"));
		Page<Issue> issues = this.gitHub.getIssues("org", "repo");
		assertThat(issues.getContent()).hasSize(15);
		assertThat(issues.next()).isNull();
	}

	@Test
	void multiplePagesOfIssues() {
		this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("issues-page-one.json", "Link:</page-two>; rel=\"next\""));
		this.server.expect(requestTo("/page-two"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("issues-page-two.json"));
		Page<Issue> pageOne = this.gitHub.getIssues("org", "repo");
		assertThat(pageOne.getContent()).hasSize(15);
		Page<Issue> pageTwo = pageOne.next();
		assertThat(pageTwo).isNotNull();
		assertThat(pageTwo.getContent()).hasSize(15);
	}

	@Test
	void multiplePagesOfIssuesWithPercentEncodedLink() {
		this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("issues-page-one.json", "Link:</page-two%3D%3D>; rel=\"next\""));
		this.server.expect(requestTo("/page-two%3D%3D"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("issues-page-two.json"));
		Page<Issue> pageOne = this.gitHub.getIssues("org", "repo");
		assertThat(pageOne.getContent()).hasSize(15);
		Page<Issue> pageTwo = pageOne.next();
		assertThat(pageTwo).isNotNull();
		assertThat(pageTwo.getContent()).hasSize(15);
	}

	@Test
	void rateLimited() {
		long reset = System.currentTimeMillis();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-RateLimit-Limit", "5000");
		headers.set("X-RateLimit-Remaining", "0");
		headers.set("X-RateLimit-Reset", Long.toString(reset / 1000));
		this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withStatus(HttpStatus.FORBIDDEN).headers(headers));
		assertThatIllegalStateException().isThrownBy(() -> this.gitHub.getIssues("org", "repo"))
			.withMessage("Rate limit exceeded. Limit will reset at " + new Date(reset));
	}

	@Test
	void noComments() {
		this.server.expect(requestTo("/commentsUrl"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		Page<Comment> comments = this.gitHub
			.getComments(new Issue(null, "/commentsUrl", null, null, null, null, null, null));
		assertThat(comments.getContent()).isEmpty();
		assertThat(comments.next()).isNull();
	}

	@Test
	void singlePageOfComments() {
		this.server.expect(requestTo("/commentsUrl"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("comments-page-one.json"));
		Page<Comment> comments = this.gitHub
			.getComments(new Issue(null, "/commentsUrl", null, null, null, null, null, null));
		assertThat(comments.getContent()).hasSize(17);
		assertThat(comments.next()).isNull();
	}

	@Test
	void multiplePagesOfComments() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Link", "<page-two>; rel=\"next\"");
		this.server.expect(requestTo("/commentsUrl"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("comments-page-one.json", "Link:</page-two>; rel=\"next\""));
		this.server.expect(requestTo("/page-two"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("comments-page-two.json"));
		Page<Comment> pageOne = this.gitHub
			.getComments(new Issue(null, "/commentsUrl", null, null, null, null, null, null));
		assertThat(pageOne.getContent()).hasSize(17);
		Page<Comment> pageTwo = pageOne.next();
		assertThat(pageTwo).isNotNull();
		assertThat(pageTwo.getContent()).hasSize(3);
	}

	@Test
	void addLabelToIssue() {
		this.server.expect(requestTo("labelsUrl"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(basicAuth())
			.andExpect(content().string("[\"test\"]"))
			.andRespond(withSuccess("[{\"name\":\"test\"}]", MediaType.APPLICATION_JSON));
		Issue issue = new Issue("issueUrl", null, null, "labelsUrl{/name}", null, null, null, null);
		Issue modifiedIssue = this.gitHub.addLabel(issue, "test");
		assertThat(modifiedIssue.getLabels()).hasSize(1);
	}

	@Test
	void removeLabelFromIssue() {
		this.server.expect(requestTo("labels/test"))
			.andExpect(method(HttpMethod.DELETE))
			.andExpect(basicAuth())
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		Issue issue = new Issue(null, null, null, "labels{/name}", null, null, null, null);
		Issue modifiedIssue = this.gitHub.removeLabel(issue, "test");
		assertThat(modifiedIssue.getLabels()).isEmpty();
	}

	@Test
	void removeLabelWithNameThatRequiresEncodingFromIssue() {
		this.server.expect(requestTo("labels/status:%20foo"))
			.andExpect(method(HttpMethod.DELETE))
			.andExpect(basicAuth())
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		Issue issue = new Issue(null, null, null, "labels{/name}", null, null, null, null);
		Issue modifiedIssue = this.gitHub.removeLabel(issue, "status: foo");
		assertThat(modifiedIssue.getLabels()).isEmpty();
	}

	@Test
	void addCommentToIssue() {
		this.server.expect(requestTo("/commentsUrl"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(basicAuth())
			.andExpect(content().string("{\"body\":\"A test comment\"}"))
			.andRespond(withResource("new-comment.json"));
		Issue issue = new Issue(null, "commentsUrl", null, null, null, null, null, null);
		Comment comment = this.gitHub.addComment(issue, "A test comment");
		assertThat(comment).isNotNull();
	}

	@Test
	void singlePageOfEvents() {
		this.server.expect(requestTo("/eventsUrl"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("events-page-one.json"));
		Page<Event> events = this.gitHub.getEvents(new Issue(null, null, "/eventsUrl", null, null, null, null, null));
		assertThat(events.getContent()).hasSize(12);
		assertThat(events.next()).isNull();
	}

	@Test
	void multiplePagesOfEvents() {
		this.server.expect(requestTo("/eventsUrl"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("events-page-one.json", "Link:</page-two>; rel=\"next\""));
		this.server.expect(requestTo("/page-two"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(basicAuth())
			.andRespond(withResource("events-page-two.json"));
		Page<Event> pageOne = this.gitHub.getEvents(new Issue(null, null, "/eventsUrl", null, null, null, null, null));
		assertThat(pageOne.getContent()).hasSize(12);
		Page<Event> pageTwo = pageOne.next();
		assertThat(pageTwo).isNotNull();
		assertThat(pageTwo.getContent()).hasSize(4);
	}

	@Test
	void closeIssueAsCompleted() {
		this.server.expect(requestTo("issueUrl"))
			.andExpect(method(HttpMethod.PATCH))
			.andExpect(basicAuth())
			.andExpect(content().json("{\"state\":\"closed\",\"state_reason\":\"completed\"}"))
			.andRespond(withSuccess("{\"url\":\"updatedIssueUrl\"}", MediaType.APPLICATION_JSON));
		Issue closedIssue = this.gitHub.close(new Issue("issueUrl", null, null, null, null, null, null, null),
				ClosureReason.COMPLETED);
		assertThat(closedIssue.getUrl()).isEqualTo("updatedIssueUrl");
	}

	@Test
	void closeIssueAsNotPlanned() {
		this.server.expect(requestTo("issueUrl"))
			.andExpect(method(HttpMethod.PATCH))
			.andExpect(basicAuth())
			.andExpect(content().json("{\"state\":\"closed\",\"state_reason\":\"not_planned\"}"))
			.andRespond(withSuccess("{\"url\":\"updatedIssueUrl\"}", MediaType.APPLICATION_JSON));
		Issue closedIssue = this.gitHub.close(new Issue("issueUrl", null, null, null, null, null, null, null),
				ClosureReason.NOT_PLANNED);
		assertThat(closedIssue.getUrl()).isEqualTo("updatedIssueUrl");
	}

	private DefaultResponseCreator withResource(String resource, String... headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		for (String header : headers) {
			String[] components = header.split(":");
			httpHeaders.set(components[0], components[1]);
		}
		return withSuccess(new UrlResource(getClass().getResource(resource)), MediaType.APPLICATION_JSON)
			.headers(httpHeaders);
	}

	private RequestMatcher basicAuth() {
		return header("Authorization",
				"Basic " + Base64.getEncoder().encodeToString("username:password".getBytes(StandardCharsets.UTF_8)));
	}

}
