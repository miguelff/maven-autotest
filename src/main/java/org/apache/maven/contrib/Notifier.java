package org.apache.maven.contrib;


/**
 * 
 * Contract that notifiers of test execution results
 * must obey
 * 
 * @author miguelff
 *
 */
public interface Notifier {

	/**
	 * @param message message to put when a test falls in error
	 */
	void putError(String message);

	/**
	 * @param message message to put when a test success
	 */
	void putSuccess(String message);

}
