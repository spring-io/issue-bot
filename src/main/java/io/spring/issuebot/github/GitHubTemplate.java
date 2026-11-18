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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.spring.issuebot.github.Issue.ClosureReason;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

/**
 * Central class for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
public class GitHubTemplate implements GitHubOperations {

	private static final Logger log = LoggerFactory.getLogger(GitHubTemplate.class);

	private final RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor();

	private final RestClient rest;

	private final LinkParser linkParser;

	/**
	 * Creates a new {@code GitHubTemplate} that will use the given {@code username} and
	 * {@code password} to authenticate, and the given {@code linkParser} to parse links
	 * from responses' {@code Link} header. The given {@code restClientBuilder} is used to
	 * create the template's underlying REST client.
	 * @param username the username
	 * @param password the password
	 * @param linkParser the link parser
	 * @param restClientBuilder the REST client builder
	 */
	public GitHubTemplate(String username, String password, LinkParser linkParser,
			RestClient.Builder restClientBuilder) {
		this.rest = createDefaultRestClient(username, password, this.rateLimitInterceptor, restClientBuilder);
		this.linkParser = linkParser;
	}

	GitHubTemplate(RestClient rest, LinkParser linkParser) {
		this.rest = rest;
		this.linkParser = linkParser;
	}

	static RestClient createDefaultRestClient(String username, String password,
			RateLimitInterceptor rateLimitInterceptor, RestClient.Builder restClientBuilder) {
		restClientBuilder.defaultStatusHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(URI uri, HttpMethod method, ClientHttpResponse response) throws IOException {
				if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
					RateLimit rateLimit = RateLimit.from(response);
					if (rateLimit.getRemaining() == 0) {
						throw new IllegalStateException(
								"Rate limit exceeded. Limit will reset at " + new Date(rateLimit.getReset()));
					}
				}
			}
		});
		restClientBuilder.defaultHeaders((httpHeaders) -> httpHeaders.setBasicAuth(username, password));
		restClientBuilder.requestInterceptor(rateLimitInterceptor);
		restClientBuilder.configureMessageConverters((clientConverters) -> clientConverters
			.withJsonConverter(new ErrorLoggingJacksonJsonHttpMessageConverter()));
		return restClientBuilder.build();
	}

	@Override
	public Page<Issue> getIssues(String organization, String repository) {
		String url = "https://api.github.com/repos/" + organization + "/" + repository + "/issues";
		return getPage(url, Issue[].class);
	}

	@Override
	public Page<Comment> getComments(Issue issue) {
		return getPage(issue.getCommentsUrl(), Comment[].class);
	}

	@Override
	public Page<Event> getEvents(Issue issue) {
		return getPage(issue.getEventsUrl(), Event[].class);
	}

	private <T> Page<T> getPage(String url, Class<T[]> type) {
		if (!StringUtils.hasText(url)) {
			return null;
		}
		ResponseEntity<T[]> contents = this.rest.get().uri(URI.create(url)).retrieve().toEntity(type);
		return new StandardPage<>(Arrays.asList(contents.getBody()), () -> getPage(getNextUrl(contents), type));
	}

	private String getNextUrl(ResponseEntity<?> response) {
		return this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
	}

	@Override
	public Issue addLabel(Issue issue, String labelName) {
		URI uri = URI.create(issue.getLabelsUrl().replace("{/name}", ""));
		log.info("Adding label {} to {}", labelName, uri);
		ResponseEntity<Label[]> response = this.rest.post()
			.uri(uri)
			.body(List.of(labelName))
			.retrieve()
			.toEntity(Label[].class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.warn("Failed to add label to issue. Response status: " + response.getStatusCode());
		}
		return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(), issue.getLabelsUrl(),
				issue.getUser(), Arrays.asList(response.getBody()), issue.getMilestone(), issue.getPullRequest());
	}

	@Override
	public Issue removeLabel(Issue issue, String labelName) {
		String encodedName;
		try {
			encodedName = new URI(null, null, labelName, null).toString();
		}
		catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		ResponseEntity<Label[]> response = this.rest.delete()
			.uri(URI.create(issue.getLabelsUrl().replace("{/name}", "/" + encodedName)))
			.retrieve()
			.toEntity(Label[].class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.warn("Failed to remove label from issue. Response status: " + response.getStatusCode());
		}
		return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(), issue.getLabelsUrl(),
				issue.getUser(), Arrays.asList(response.getBody()), issue.getMilestone(), issue.getPullRequest());
	}

	@Override
	public Comment addComment(Issue issue, String comment) {
		Map<String, String> body = new HashMap<>();
		body.put("body", comment);
		return this.rest.post().uri(issue.getCommentsUrl()).body(body).retrieve().body(Comment.class);
	}

	@Override
	public Issue close(Issue issue, ClosureReason closureReason) {
		Map<String, String> body = new HashMap<>();
		body.put("state", "closed");
		if (closureReason != null) {
			body.put("state_reason", closureReason.getStateReason());
		}
		ResponseEntity<Issue> response = this.rest.patch()
			.uri(issue.getUrl())
			.body(body)
			.retrieve()
			.toEntity(Issue.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.warn("Failed to close issue. Response status: " + response.getStatusCode());
		}
		return response.getBody();
	}

	@Override
	public RateLimit getRateLimit() {
		return this.rateLimitInterceptor.rateLimit;
	}

	RestClient getRestClient() {
		return this.rest;
	}

	private static final class ErrorLoggingJacksonJsonHttpMessageConverter extends JacksonJsonHttpMessageConverter {

		private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

		@Override
		public Object read(ResolvableType type, HttpInputMessage inputMessage, @Nullable Map<String, Object> hints)
				throws IOException, HttpMessageNotReadableException {
			try {
				return super.read(type, inputMessage, hints);
			}
			catch (HttpMessageNotReadableException ex) {
				log.error("Failed to create {} from {}", type.getRawClass().getName(), read(inputMessage), ex);
				throw ex;
			}
		}

		private String read(HttpInputMessage inputMessage) throws IOException {
			return StreamUtils.copyToString(inputMessage.getBody(), CHARSET_UTF_8);
		}

	}

	private static final class RateLimitInterceptor implements ClientHttpRequestInterceptor {

		private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

		private volatile RateLimit rateLimit = null;

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			ClientHttpResponse response = execution.execute(request, body);
			try {
				this.rateLimit = RateLimit.from(response);
			}
			catch (Exception ex) {
				log.warn("Rate limit unavailable from response with headers {}", response.getHeaders());
				this.rateLimit = null;
			}
			return response;
		}

	}

}
