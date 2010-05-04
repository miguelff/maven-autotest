package org.apache.maven.contrib;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public final class TestInvoker {

	/**
	 * List of notifiers, each of one will notify the user the result of
	 * executing it's containing tests.
	 */
	private List<Notifier> notifiers;

	private Invoker invoker;

	protected final InvocationRequest request;
	
	private SharedBufferInvocationOutputHandler errorHandler, outputHandler;

	public TestInvoker(List<Notifier> notifiers) {
		this.notifiers = notifiers;
		this.invoker = new DefaultInvoker();
		this.errorHandler=new SharedBufferInvocationOutputHandler();
		this.outputHandler=new SharedBufferInvocationOutputHandler();
		this.request = new DefaultInvocationRequest();
		initializeInvocationRequest();
	}

	private void initializeInvocationRequest() {
		request.setShowErrors(true);
		request.setGoals(Arrays.asList(new String[] { "test" }));
		request.setErrorHandler(errorHandler);
		request.setOutputHandler(outputHandler);
	}

	public void test(File file) {
		Properties props = new Properties();
		request.setProperties(props);
		//Must fix: see TODO.markdown Fix#1
		props.setProperty("test", getNormalizedFileName(file));
		
		InvocationResult result;
		try {
			result = invoker.execute(request);
		} catch (MavenInvocationException e) {
			throw new RuntimeException(e);
		}

		if (result.getExitCode() != 0) {
			for (Notifier notifier : notifiers) {
				notifier.putError(file,outputHandler.gets());
			}
		} else {// if (!skipGreen){
			for (Notifier notifier : notifiers) {
				notifier.putSuccess(file,outputHandler.gets());
			}
		}
		errorHandler.clear();
		outputHandler.clear();
	}

	//Must fix: see TODO.markdown Fix#1
	private String getNormalizedFileName(File f) {
		String path=f.getPath();
		return path.substring(path.lastIndexOf("/")+1,path.length());
	}

	class SharedBufferInvocationOutputHandler implements InvocationOutputHandler{

		StringBuilder builder=new StringBuilder();
		
		public void consumeLine(String line) {
			builder.append(line+"\n");
		}
		
		public void clear() {
			builder=new StringBuilder();
		}
		
		public String gets(){
			return builder.toString();
		}
		
		public String toString(){
			return gets();
		}
	}
	
}
