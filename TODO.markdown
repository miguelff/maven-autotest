##FIXME:

* [In progress] Fixme #1: When two files with the same name coexist in different directories (i.e. src/test/java/integration/Foo.java and src/test/java/unit/Foo.java) . A change in one of them will trigger the test of both.
This happens because surefire plugin can receive a single file name to test that must be a qualified file name relative src/test/XXX , where XXX is the language in which the test is coded.
The problem is that FileChangeDetector provides qualified names relative to the project root (at the same level of pom.xml) and depending on the file patterns observed, could notify AutoTestMojo changes in files within different folders
(e.g. src/test/java, src/test/scala). A possible workaround could be replacing the implementation of TestInvoker#getNormalizedFileName to return the result of ridding of the ${project.root}/src/test/XXX part of the filename. 

##TODO:

* Todo #1: provide a javadoc documentation
* Todo #2: push the test cases (once they are coded in a way they were self contained (don't depend on third-party projects)).