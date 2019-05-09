# Issue Bot for GitHub issues

The issue bot helps manage open GitHub issues.

## Features

The issue bot helps by managing [Waiting for Triage](waiting-for-triage) and monitoring [Waiting for Feedback](waiting-for-feedback).

### Waiting for Triage

The issue bot labels issues and pull requests as waiting for triage.
An issue or pull request is deemed as waiting for triage when all of the following is true:
* It is open
* It wasn’t opened by a collaborator
* It isn’t assigned to a milestone
* It has no labels.

When the issue is no longer waiting for triage, the label should be manually removed.

### Waiting for Feedback

The issue bot monitors open issues (not pull requests) that are labeled as waiting for feedback.

* If you apply the waiting for feedback label, the bot will then monitor the issue.
* If feedback isn’t provided within 7 days of the label being applied, it’ll comment with a reminder.
* If feedback isn’t provided within a futher 7 days, it’ll close the issue.

Any comment that isn’t from a collaborator is considered to be feedback.
When such a comment is made, the bot removes the waiting label and adds a feedback provided label.

## Monitoring a Repository

Configured repositories are scanned periodically.
To add a repository:

* First ensure the repository has the labels:
  * `status: waiting-for-feedback`
  * `status: feedback-provided`
  * `status: feedback-reminder`
* Then send a pull request similar to https://github.com/spring-io/issue-bot/pull/12
