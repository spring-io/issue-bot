package io.spring.issuebot;

import io.spring.issuebot.github.Issue;

@FunctionalInterface
public interface IssueOperation {

  void run(Issue issue, IssueListener issueListener);
}
