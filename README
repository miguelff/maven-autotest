This is a maven-autotest a ZenTest-like maven plugin for continous testing.

It uses surefire-test plugin to continuously test compilation units when a change on any of them is detected.  

##Available goals

* autotest:run

##Overriding default behavior

By default maven-autotest will track changes on *Test.java files inside src/test directory.

It is possible to modify default behavior by telling autotest to track and test other files that match a certain *java.util.Regexp* pattern. 

For instance, to track and test both java and scala tests in the console:

	$mvn autotest:run -Dincludes=.*Test.java,.*Test.scala

By default, maven-autotest will notify test results on the console, but other appenders could be specified. (They must be classpath compilation units
implementing org.apache.maven.contrib.Notifier)

For instance, if com.MyNotifier and com.JToastNotifier where Notifier subclasses accessible from the classpath, we could use it this way.
	
	$mvn autotest:run -Dnotifiers=com.MyNotifier, com.JToastNotifier
	
You can also override the default (2 seconds) lookup frequency, and set it for instance at a 10-second interval:

	$mvn autotest:run -Dlookup.frequency=10
