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
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.spring.issuebot.github.Issue.ClosureReason;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
public class GitHubTemplate implements GitHubOperations {

	private static final Logger log = LoggerFactory.getLogger(GitHubTemplate.class);

	private final RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor();

	private final RestOperations rest;

	private final LinkParser linkParser;

	/**
	 * Creates a new {@code GitHubTemplate} that will use the given {@code username} and
	 * {@code password} to authenticate, and the given {@code linkParser} to parse links
	 * from responses' {@code Link} header.
	 * @param username the username
	 * @param password the password
	 * @param linkParser the link parser
	 */
	public GitHubTemplate(String username, String password, LinkParser linkParser) {
		this.rest = createDefaultRestTemplate(username, password, this.rateLimitInterceptor);
		this.linkParser = linkParser;
	}

	GitHubTemplate(RestOperations rest, LinkParser linkParser) {
		this.rest = rest;
		this.linkParser = linkParser;
	}

	static RestTemplate createDefaultRestTemplate(String username, String password,
			RateLimitInterceptor rateLimitInterceptor) {
		RestTemplate rest = new RestTemplate();
		rest.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
					RateLimit rateLimit = RateLimit.from(response);
					if (rateLimit.getRemaining() == 0) {
						throw new IllegalStateException(
								"Rate limit exceeded. Limit will reset at " + new Date(rateLimit.getReset()));
					}
				}
			}
		});
		HttpClient httpClient = HttpClientBuilder.create()
			.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofSeconds(30)).build())
				.build())
			.build();
		BufferingClientHttpRequestFactory bufferingClient = new BufferingClientHttpRequestFactory(
				new HttpComponentsClientHttpRequestFactory(httpClient));
		rest.setRequestFactory(bufferingClient);
		rest.setInterceptors(
				Arrays.asList(new BasicAuthorizationInterceptor(username, password), rateLimitInterceptor));
		rest.setMessageConverters(Collections.singletonList(new ErrorLoggingMappingJackson2HttpMessageConverter()));
		return rest;
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
		ResponseEntity<T[]> contents = this.rest.getForEntity(URI.create(url), type);
		return new StandardPage<>(Arrays.asList(contents.getBody()), () -> getPage(getNextUrl(contents), type));
	}

	private String getNextUrl(ResponseEntity<?> response) {
		return this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
	}

	@Override
	public Issue addLabel(Issue issue, String labelName) {
		URI uri = URI.create(issue.getLabelsUrl().replace("{/name}", ""));
		log.info("Adding label {} to {}", labelName, uri);
		ResponseEntity<Label[]> response = this.rest
			.exchange(new RequestEntity<>(Collections.singletonList(labelName), HttpMethod.POST, uri), Label[].class);
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
		ResponseEntity<Label[]> response = this.rest.exchange(new RequestEntity<>(HttpMethod.DELETE,
				URI.create(issue.getLabelsUrl().replace("{/name}", "/" + encodedName))), Label[].class);
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
		return this.rest.postForEntity(issue.getCommentsUrl(), body, Comment.class).getBody();
	}

	@Override
	public Issue close(Issue issue, ClosureReason closureReason) {
		Map<String, String> body = new HashMap<>();
		body.put("state", "closed");
		if (closureReason != null) {
			body.put("state_reason", closureReason.getStateReason());
		}
		ResponseEntity<Issue> response = this.rest
			.exchange(new RequestEntity<>(body, HttpMethod.PATCH, URI.create(issue.getUrl())), Issue.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.warn("Failed to close issue. Response status: " + response.getStatusCode());
		}
		return response.getBody();
	}

	@Override
	public RateLimit getRateLimit() {
		return this.rateLimitInterceptor.rateLimit;
	}

	RestOperations getRestOperations() {
		return this.rest;
	}

	private static final class ErrorLoggingMappingJackson2HttpMessageConverter
			extends MappingJackson2HttpMessageConverter {

		private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

		@Override
		public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			try {
				return super.read(type, contextClass, inputMessage);
			}
			catch (HttpMessageNotReadableException ex) {
				log.error("Failed to create {} from {}", type.getTypeName(), read(inputMessage), ex);
				throw ex;
			}
		}

		private String read(HttpInputMessage inputMessage) throws IOException {
			return StreamUtils.copyToString(inputMessage.getBody(), CHARSET_UTF_8);
		}

	}

	private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor {

		private static final Charset UTF_8 = StandardCharsets.UTF_8;

		private final String username;

		private final String password;

		BasicAuthorizationInterceptor(String username, String password) {
			this.username = username;
			this.password = (password != null) ? password : "";
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			String token = Base64.getEncoder().encodeToString((this.username + ":" + this.password).getBytes(UTF_8));
			request.getHeaders().add("Authorization", "Basic " + token);
			return execution.execute(request, body);
		}

	}

	private static class RateLimitInterceptor implements ClientHttpRequestInterceptor {

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
