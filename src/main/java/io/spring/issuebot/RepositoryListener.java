package io.spring.issuebot;

/**
 * A {@code RepositoryListener} is called when monitoring a repository
 */
public interface RepositoryListener {

	/**
	 * Handle the specific repository
	 * @param repository the repository
	 */
	void handle(Repository repository);

}
