Test
====

Tests are build like unit tests but they aren't 'unit' at all.  
The framework used is TestNG.  
You can test each part ( client and server ) separetely (with other clien/server implementations) or both together.  
The server test is a *fake test*, it will never fail, a timeout is set to avoid infinite test.

I'll explain how to run theses tests with maven and eclipse. For eclipse I'll suppose you have imported jwamp as a project and have TestNG plugin installed.  

To run test separetely on maven use -Dtest parameter : 

    mvn -Dtest=TestClient test

or 

    mvn -Dtest=TestServer test

*If you run TestServer alone from maven it will not have timeout set.*  
On eclipse, right-click on the class file and select "run as TestNG Test".

To run all tests on maven use :

    mvn test

In eclipse you can  
* right-click on "srv/test/resources/testng.xml" file and select "run as TestNG Suite".  
or if you have m2eclipse you can  
* right-click on the project and select "run as Maven test".

Server Actions
---------------

|Action type | Action Id  | Arguments                       | Effect                           | Return      | 
|:----------:|:----------:|:-------------------------------:|:--------------------------------:|:-----------:|
|RPC         |  CallTest  |                                 |                                  | "SUCCEED"   |
|RPC         |    Manage  |  restart                        |  restart the server              |             |
|            |            | shutdown(optional)              | shutdown the server              |             |
|Event       | EventTest  |                                 |Send Event right after subscribe  |"EventAction"|

