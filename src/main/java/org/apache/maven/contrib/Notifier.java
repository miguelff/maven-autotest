package org.apache.maven.contrib;

import java.io.File;

import org.apache.maven.plugin.logging.Log;


/**
 * 
 * Contract that notifiers of test execution results
 * must obey
 * 
 * @author miguelff
 *
 */
public interface Notifier {
	
	void putError(File testCase, String explanation);


	void putSuccess(File testCase, String explanation);

	public static class LogNotifier implements Notifier{

		Log log;
		
		protected LogNotifier(Log log){
			this.log=log;
		}
		
		public void putError(File f, String explanation) {
			log.error(f.getName()+ "FAILED");
			log.error(explanation);
			
		}

		public void putSuccess(File f, String explanation) {
			log.info(f.getName()+ "PASSED");
			log.info(explanation);
			
		}
	}
}
