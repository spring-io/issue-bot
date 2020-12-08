/*
 * Copyright 2015-2020 the original author or authors.
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

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;

/**
 * The current rate limit for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
public final class RateLimit {

	private static final String HEADER_REMAINING = "X-RateLimit-Remaining";

	private static final String HEADER_LIMIT = "X-RateLimit-Limit";

	private static final String HEADER_RESET = "X-RateLimit-Reset";

	private final int limit;

	private final int remaining;

	private final long reset;

	private RateLimit(int limit, int remaining, long reset) {
		this.limit = limit;
		this.remaining = remaining;
		this.reset = reset;
	}

	static RateLimit from(ClientHttpResponse response) {
		HttpHeaders headers = response.getHeaders();
		int remaining = Integer.parseInt(headers.getFirst(HEADER_REMAINING));
		int limit = Integer.parseInt(headers.getFirst(HEADER_LIMIT));
		long reset = Long.parseLong(headers.getFirst(HEADER_RESET)) * 1000;
		return new RateLimit(limit, remaining, reset);
	}

	/**
	 * Returns the maximum number of requests permitted per rate limit window.
	 * @return the requests limit
	 *
	 */
	public int getLimit() {
		return this.limit;
	}

	/**
	 * Returns the number of remaining number of requests that will be permitted in the
	 * current rate limit window.
	 * @return the remaining number of requests
	 */
	public int getRemaining() {
		return this.remaining;
	}

	/**
	 * The time, in milliseconds since the epoch, at which the rate limit window will
	 * reset.
	 * @return the window reset time
	 */
	public long getReset() {
		return this.reset;
	}

}
