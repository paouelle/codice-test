# Definalizer
The Definalizer JUnit test runner is designed as a generic proxy test runner for another JUnit test runner by indirectly instantiating that runner in order to add support for de-finalizing (i.e. removing the final constraint) 3rd party Java classes that need to be mocked or stubbed during testing. 
It does so by creating a classloader designed with an aggressive strategy where it will load all classes first before delegating to its parent. This classloader will therefore reload all classes while definalizing those that are requested except for all classes in the following packages:
* java
* javax
* sun
* org.xml
* org.junit

These packages are not being reloaded as they are required for this test runner to delegate to the real test runner. Even the actual test class will be reloaded in this internal classloader.

The indirect extension is done by means of delegation as the real test runner is instantiated from within the classloader that is created internally. 
This is to ensure that everything the test class, the test runner, and everything they indirectly reference are loaded from within the classloader.

This runner is especially useful with Spock where it is not possible to mock final methods as can be done with Mockito.

The `@Definalize` annotation should be added to the test class to specify which classes and/or packages to definalize.

The `@DefinalizeWith` annotation can be added to specify which actual test runner should be used to run the test case. By default, it will either use the standard JUnit or Sputnik test runner.