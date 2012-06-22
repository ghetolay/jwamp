Test
====

Tests are build like unit tests but they aren't 'unit' at all.  
The framework used is TestNG.  
You can test each part client and server separetely (with other clien/server implementations) or together.  
The server test is a *fake test*, it will never fail, a timeout is set to avoid infinite test.

I'll explain how to run theses test with maven and eclipse. For eclipse I'll suppose you have imported jwamp as a project and installed TestNG plugin.  

To run them separetely on maven use -Dtest parameter : 

    mvn -Dtest=TestClient test

or 

    mvn -Dtest=TestServer test

On eclipse, right-click on the class file and select "run as TestNG Test".

To run both tests on maven :

    mvn test

In eclipse you can 
*right-click on "srv/test/resources/testng.xml" file and select "run as TestNG Suite".
or if you have m2eclipse you can 
*right-click on the project and select "run as Maven test".

Server Behavior
---------------

|Action type | Action Id  | Arguments                       | Effect                           | Return    | 
|:----------:|:----------:|:-------------------------------:|:--------------------------------:|:---------:|
|RPC         |  CallTest  |                                 |                                  |  SUCCEED  |
|RPC         |    Manage  |  restart                        |  restart the server              |           |
|            |            | shutdown(optional)              | shutdown the server              |           |
|Event       | EventTest  |                                 |Send Event right after subscribe  |EventAction|

