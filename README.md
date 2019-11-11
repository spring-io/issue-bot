# Issue Bot for GitHub issues

The issue bot helps manage open GitHub issues.

## Features

The issue bot helps by managing [Waiting for Triage](waiting-for-triage) and monitoring [Waiting for Feedback](waiting-for-feedback).

### Waiting for Triage

The issue bot labels issues and pull requests as waiting for triage.
An issue or pull request is deemed as waiting for triage when all of the following is true:
* It is open
* It was not opened by a collaborator
* It is not assigned to a milestone
* It has no labels.

When the issue is no longer waiting for triage, the label should be manually removed.

### Waiting for Feedback

The issue bot monitors open issues and pull requests that are labeled as waiting for
feedback. Monitoring begins when the waiting for feedback label is applied. Any comment
that is not from a collaborator is considered to be feedback.

For issues, the bot will:

* Comment with a reminder of feedback is not provided within 7 days of the label being
  applied.
* Close the issue if feedback is not provided within a further 7 days.
* Add the feedback provided label and remove the waiting for feedback label when feedback
  is provided.

For pull requests, the bot will:

* Add the feedback provided label and remove the waiting for feedback label when feedback
  is provided.

## Monitoring a Repository

Configured repositories are scanned periodically.
To add a repository:

* First ensure the repository has the labels:
  * `status: waiting-for-feedback`
  * `status: feedback-provided`
  * `status: feedback-reminder`
* Then send a pull request similar to https://github.com/spring-io/issue-bot/pull/12
