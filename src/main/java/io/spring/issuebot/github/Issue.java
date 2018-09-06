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

package io.spring.issuebot.github;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A GitHub issue.
 *
 * @author Andy Wilkinson
 */
public class Issue {

	@Getter(AccessLevel.PACKAGE)
	private final String url;

	@Getter(AccessLevel.PACKAGE)
	private final String commentsUrl;

	@Getter(AccessLevel.PACKAGE)
	private final String eventsUrl;

	@Getter(AccessLevel.PACKAGE)
	private final String labelsUrl;

	@Getter
	private final String repositoryUrl;

	@Getter
	private final User user;

	@Getter
	private List<Label> labels;

	@Getter
	private final Milestone milestone;

	@Getter
	private final PullRequest pullRequest;

	/**
	 * Creates a new {@code Issue}.
	 *  @param url the url of the issue in the GitHub API
	 * @param commentsUrl the url of the comments on the issue in the GitHub API
	 * @param eventsUrl the url of the events on the issue in the GitHub API
	 * @param labelsUrl the url of the labels on the issue in the GitHub API
	 * @param repositoryUrl the url of the repository on the issue in the GitHub API
	 * @param user the user that created the issue
	 * @param labels the labels applied to the issue
	 * @param milestone the milestone applied to the issue
	 * @param pullRequest details of the pull request (if this issue is a pull request)
	 */
	@JsonCreator
	public Issue(@JsonProperty("url") String url,
			@JsonProperty("comments_url") String commentsUrl,
			@JsonProperty("events_url") String eventsUrl,
			@JsonProperty("labels_url") String labelsUrl,
			@JsonProperty("repository_url") String repositoryUrl,
			@JsonProperty("user") User user,
			@JsonProperty("labels") List<Label> labels,
			@JsonProperty("milestone") Milestone milestone,
			@JsonProperty("pull_request") PullRequest pullRequest) {
		this.url = url;
		this.commentsUrl = commentsUrl;
		this.eventsUrl = eventsUrl;
		this.labelsUrl = labelsUrl;
		this.repositoryUrl = repositoryUrl;
		this.user = user;
		this.labels = labels;
		this.milestone = milestone;
		this.pullRequest = pullRequest;
	}

	@Override
	public String toString() {
		return this.url;
	}

	/**
	 * Creates slug (org/repo) from repositoryUrl.
	 * @return if repo url is not null, slug, otherwise null
	 */
	public Slug slug() {
		String repositoryUrl = getRepositoryUrl();
		if (repositoryUrl == null) {
			return null;
		}
		return Slug.from(repositoryUrl);
	}

	/**
	 * Org and repo pair that represent a github project.
	 */
	@Data
	public static class Slug {

		private final String org;
		private final String repo;

		/**
		 * Finds a value in a map based on slug, then repo, then org.
		 * @param map The map to search.
		 * @param <V> the type of the value.
		 * @return The value or null.
		 */
		public <V> V find(Map<String, V> map) {
			if (map.containsKey(toString())) {
				return map.get(toString());
			}
			else if (map.containsKey(getRepo())) {
				return map.get(getRepo());
			}
			else if (map.containsKey(getOrg())) {
				return map.get(getOrg());
			}
			return null;
		}

		/**
		 * Formats org and repo.
		 * @return org/repo
		 */
		@Override
		public String toString() {
			return this.org + "/" + this.repo;
		}

		/**
		 * Factory method for creating slug from url.
		 * @param repositoryUrl url to parse.
		 * @return A Slug from the url.
		 */
		public static Slug from(String repositoryUrl) {
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(repositoryUrl).build();
			List<String> segments = uriComponents.getPathSegments();
			String repo = segments.get(segments.size() - 1);
			String org = segments.get(segments.size() - 2);
			return new Slug(org, repo);
		}

	}
}
