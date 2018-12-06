/*
 * Copyright 2015-2018 the original author or authors.
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

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event that has been performed on an {@link Issue}.
 *
 * @author Andy Wilkinson
 */
public class Event {

	private static final Logger log = LoggerFactory.getLogger(Event.class);

	private final Type type;

	private final OffsetDateTime creationTime;

	private final Label label;

	/**
	 * Creates a new {@code Event}.
	 * @param type the type of the event
	 * @param creationTime the timestamp of when the event was created
	 * @param label the label associated with the event
	 */
	@JsonCreator
	public Event(@JsonProperty("event") String type,
			@JsonProperty("created_at") OffsetDateTime creationTime,
			@JsonProperty("label") Label label) {
		this.type = Type.valueFrom(type);
		this.creationTime = creationTime;
		this.label = label;
	}

	public Type getType() {
		return this.type;
	}

	public OffsetDateTime getCreationTime() {
		return this.creationTime;
	}

	public Label getLabel() {
		return this.label;
	}

	/**
	 * The type of an {@link Event}.
	 *
	 * @author Andy Wilkinson
	 */
	public enum Type {

		/**
		 * The issue was added to a project board.
		 */
		ADDED_TO_PROJECT("added_to_project"),

		/**
		 * The issue was assigned to the actor.
		 */
		ASSIGNED("assigned"),

		/**
		 * The issue was closed by the actor.
		 */
		CLOSED("closed"),

		/**
		 * A comment on the issue has been deleted by the actor.
		 */
		COMMENT_DELETED("comment_deleted"),

		/**
		 * The issue was created by converting a note in a project board to an issue.
		 */
		CONVERTED_NOTE_TO_ISSUE("converted_note_to_issue"),

		/**
		 * The issue was removed from a milestone.
		 */
		DEMILESTONED("demilestoned"),

		/**
		 * The pull request's branch was deleted.
		 */
		HEAD_REF_DELETED("head_ref_deleted"),

		/**
		 * The pull request's branch was restored.
		 */
		HEAD_REF_RESTORED("head_ref_restored"),

		/**
		 * A label was added to the issue.
		 */
		LABELED("labeled"),

		/**
		 * The issue was locked by the actor.
		 */
		LOCKED("locked"),

		/**
		 * The issue was marked as a duplicate.
		 */
		MARKED_AS_DUPLICATE("marked_as_duplicate"),

		/**
		 * The actor was {@code @mentioned} in an issue body.
		 */
		MENTIONED("mentioned"),

		/**
		 * The issue was merged by the actor.
		 */
		MERGED("merged"),

		/**
		 * The issue was added to a milestone.
		 */
		MILESTONED("milestoned"),

		/**
		 * The issue was moved between columns in a project board.
		 */
		MOVED_COLUMNS_IN_PROJECT("moved_columns_in_project"),

		/**
		 * The issue was referenced from a commit message.
		 */
		REFERENCED("referenced"),

		/**
		 * The issue was removed from a project board.
		 */
		REMOVED_FROM_PROJECT("removed_from_project"),

		/**
		 * The issue title was changed.
		 */
		RENAMED("renamed"),

		/**
		 * The issue was reopened by the actor.
		 */
		REOPENED("reopened"),

		/**
		 * The actor dismissed a review from the pull request.
		 */
		REVIEW_DISMISSED("review_dismissed"),

		/**
		 * The actor requested a review from the subject on the pull request.
		 */
		REVIEW_REQUESTED("review_requested"),

		/**
		 * The actor removed the review request from the subject on the pull request.
		 */
		REVIEW_REQUEST_REMOVED("review_request_removed"),

		/**
		 * The actor subscribed to receive notifications for an issue.
		 */
		SUBSCRIBED("subscribed"),

		/**
		 * The actor was unassigned from the issue.
		 */
		UNASSIGNED("unassigned"),

		/**
		 * A label was removed from the issue.
		 */
		UNLABELED("unlabeled"),

		/**
		 * The issue was unlocked by the actor.
		 */
		UNLOCKED("unlocked"),

		/**
		 * The issue was unmarked as a duplicate.
		 */
		UNMARKED_AS_DUPLICATE("unmarked_as_duplicate"),

		/**
		 * The actor unsubscribed from receiving notifications for an issue.
		 */
		UNSUBSCRIBED("unsubscribed");

		private final String type;

		Type(String type) {
			this.type = type;
		}

		static Type valueFrom(String type) {
			for (Type value : values()) {
				if (type.equals(value.type)) {
					return value;
				}
			}
			if (log.isInfoEnabled()) {
				log.info("Received unknown event type '" + type + "'");
			}
			return null;
		}

	}

}
