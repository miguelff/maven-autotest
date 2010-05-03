package org.apache.maven.contrib;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Starts up a daemon that runs tests once any change on their sources happens
 *
 * @goal run
 * 
 */
public class AutotestMojo extends AbstractMojo implements Observer
{
    /**
     * Frequency for looking for changes in test files.
     * 
     * @parameter expression="${lookup.frequency}"
     * 			  alias="lookup.frequency"
     * 			  default-value=2
     * 
     */
    private int seconds;
    
    /**
     * List of qualified classnames (separated by commnas) used to specify the appender classes 
     * that will be used to notify test results.
     * 
     * Each appender class must implement the {@link Notifier} contract
     * 
     * @parameter expression="${notifiers}"
     * 			  alias="notifiers"
     */
    private String notifierClasses;
    
    /**
     * 
     * List of regular expressions (separated by commas) used to specify the files that should be included in testing. When not
     * specified and when the parameter is not specified, the default includes will be
     * <code>**&#47;Test*.java **&#47;*Test.java **&#47;*TestCase.java</code>. 
     * 
     * @parameter expression="${includes}"
     * 			  alias="includes"
     * 			  default-value=".*Test.java"
     * 
     */
    private String includes;
    
    /**
     * 
     * List of regular expressions (separated by commas) used to specify the files that should be excluded in testing. When not
     * specified and when the parameter is not specified, no files will be excluded
     * 
     * @parameter expression="${excludes}"
     * 			  alias="excludes"
     * 
     */
    private String excludes;
    
    
    private FileChangeDetector changeDetector;
    
    private TestInvoker testInvoker;
    

    private void initialize() {
		initializeTestInvoker();
		initializeChangeDetector();
	}

	

	private void initializeTestInvoker() {
		String[] notifierClassNames= (notifierClasses!=null) ? notifierClasses.split(",") : new String[]{};
		List<Notifier> notifiers=new ArrayList<Notifier>();
		//CoC
		notifiers.add(new Notifier.LogNotifier(getLog()));
		for (String notifierClass: notifierClassNames){
			try {
				notifiers.add((Notifier)Class.forName(notifierClass.trim()).newInstance());
			} catch (Throwable t) {
				getLog().warn("could not load notifier class", t);
			} 
		}
		this.testInvoker=new TestInvoker(notifiers);
	}
	
	private void initializeChangeDetector() {
		String[] includeRegexps=includes.split(",");
		Set<Pattern> acceptedFiles=new HashSet<Pattern>();
		for (String acceptedFileRegexp : includeRegexps){
			try{
				acceptedFiles.add(Pattern.compile(acceptedFileRegexp));
			}catch (Throwable t){
				getLog().warn("can't compile regexp: "+acceptedFileRegexp,t);
			}
		}
		
		String[] excludeRegexps= (excludes!=null) ? excludes.split(","): new String[]{};
		Set<Pattern> rejectedFiles=new HashSet<Pattern>();
		for (String rejectedFileRegexp : excludeRegexps){
			try{
				acceptedFiles.add(Pattern.compile(rejectedFileRegexp ));
			}catch (Throwable t){
				getLog().warn("can't compile regexp: "+rejectedFileRegexp ,t);
			}
		}
		changeDetector=new FileChangeDetector(acceptedFiles,rejectedFiles, new File("src/test"));
		changeDetector.addObserver(this);
	}
    
    public void execute()
        throws MojoExecutionException
    {
       initialize();
       getLog().info("Starting daemon... Press Ctrl+C to stop execution");
       changeDetector.start();
       while (true){
			changeDetector.checkForChanges();
    	   try {
    		   Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
       }
    }

	
    /**
     * Receives observable notifications containing information about what tracked files have been changed
     * For each changed file invokes surefire-test plugin in order to test it.
     */
	@SuppressWarnings("unchecked")
	public void update(Observable arg0, Object uncastedArgs) {
		HashMap<String,Object> args= (HashMap)uncastedArgs;
		Set<File> changedFiles=(Set)args.get("files");
		for (File file: changedFiles){
			testInvoker.test(file);
		}	
	}
	

	
}
