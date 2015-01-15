## Do not use. The plugin is still under development.

## Grails plugin for integrating Drools

[Drools](https://www.drools.org) is a Business Rules Management System (BRMS) solution. The plugin fully supports [Drools kie-spring integration](http://docs.jboss.org/drools/release/6.1.0.Final/drools-docs/html/ch.kie.spring.html).

The plugin has been tested in the following environment:

* Drools 6.2.0.CR4

* OSX 10.10

* JDK 1.7.0_71

<!--* Grails versions 2.0.4, 2.1.5, 2.2.4, 2.3.9, and 2.4.4-->

If you have a question, problem, suggestion, or want to report a bug, please submit an [issue](https://github.com/kensiprell/grails-drools/issues?state=open). I will reply as soon as I can.

[Release Notes](https://github.com/kensiprell/grails-drools/wiki/Release-Notes)

## How the Plugin Works
The plugin offers a variety of ways to use rules. The [RuleTests](https://github.com/kensiprell/grails-drools/blob/master/test/integration/grails/plugin/drools/RulesTests.groovy) class shows several examples.

### Beans
You can define beans using either a configuration file ```grails-app/conf/DroolsConfig.groovy``` or an xml file ```grails-app/conf/drools-context.xml```. This will allow you to do something like this:

```
class SomeService {
	def packageOneStatelessSession
	
	def someMethod() {
		def fact1 = SomeDomain.get(123)
		def fact2 = SomeOtherDomain.get(123)
		Object [] facts = [fact1, fact2]
		packageOneStatelessSession.execute(Arrays.asList(facts))
	}
```

### Database and File Rules
The [DroolsService](https://github.com/kensiprell/grails-drools/blob/master/grails-app/services/grails/plugin/drools/DroolsService.groovy) offers several methods to use rules stored in a database or on the file system (classpath). For example, assuming you have several rules in the database with a ```packageName``` of "ticket", you could do something like this:

```
class SomeOtherService {
	def droolsService
	
	def someMethod() {
		def fact1 = SomeDomain.get(123)
		def fact2 = SomeOtherDomain.get(123)
		droolsService.fireFromDatabase("ticket", [fact1, fact2])
	}
```

## Plugin Installation, Configuration, and Use

### Installation
Edit your ```BuildConfig.groovy```:

```
plugins {
 // other plugins
 compile ":drools:0.4.1"
 // other plugins
}
```

### Configuration
Although Grails prefers convention over configuration, you cannot avoid some configuration for the Drools beans. 

After the plugin is installed you will find a heavily commented ```grails-app/conf/DroolsConfig.groovy``` that you can use as a starting point for configuring your beans. When your application is compiled this file is parsed and ```grails-app/conf/drools-context.xml``` is created (or overwritten). For example, [DroolsTestConfig.groovy](https://github.com/kensiprell/grails-drools/blob/master/grails-app/conf/DroolsTestConfig.groovy) is the one used for plugin integration tests.

There are two options that you can configure in your ```grails-app/conf/BuildConfig.groovy```. The defaults are shown below:

```
grails.plugin.drools.configurationType = "droolsConfigGroovy"
grails.plugin.drools.drlFileLocation = "src/rules"
```

#### grails.plugin.drools.configurationType
The option below will stop the plugin from overwriting ```grails-app/conf/drools-context.xml```. This will allow you to edit the file manually without losing changes.

```
grails.plugin.drools.configurationType = "droolsContextXml"
```


#### grails.plugin.drools.drlFileLocation
This option is the directory root for Rule files, those files with a "drl" or "rule" suffix. Note the lack of leading and trailing slashes below:

```
grails.plugin.drools.drlFileLocation = "path/to/my/rules"
```

You can take advantage of rule packages by creating subdirectories under ```drlFileLocation```. See the plugin's [src/rules](https://github.com/kensiprell/grails-drools/tree/master/src/rules) for an example.

All files in this directory and its subdirectories with a "drl" or "rule" suffix will be copied to the classpath.

#### DroolsConfig.groovy
Setting the ```includeInConfig = false``` property will prevent that item from being included in ```grails-app/conf/drools-context.xml```. This will allow you to exclude it without having to delete or comment out its entire section. All other items are described in [Drools Spring Integration](http://docs.jboss.org/drools/release/6.1.0.Final/drools-docs/html/ch.kie.spring.html).



Note that changing the ```grails.plugin.drools.drlFileLocation``` option could affect the ```packages``` property for a ```KieBase```. For example, for the option

```
grails.plugin.drools.drlFileLocation = "path/to/myRules"
```

the property would be:

```
packages: "myRules.package1"
```


### Scripts
The plugin offers three command-line scripts.

#### create-drools-config
TODO

#### create-drools-context
TODO

#### create-drools-domain
TODO

### Event Listeners
TODO 

<!--Drools supports adding three types of listeners to KieSessions - AgendaListener, WorkingMemoryListener, ProcessEventListener
The kie-spring module allows you to configure these listeners to KieSessions using XML tags. These tags have identical names as the actual listener interfaces i.e., <kie:agendaEventListener....>, <kie:ruleRuntimeEventListener....> and <kie:processEventListener....>.
kie-spring provides features to define the listeners as standalone (individual) listeners and also to define them as a group.-->


### Logging
TODO

<!--Drools supports adding 2 types of loggers to KieSessions - ConsoleLogger, FileLogger.

The kie-spring module allows you to configure these loggers to KieSessions using XML tags. These tags have identical names as the actual logger interfaces i.e., <kie:consoleLogger....> and <kie:fileLogger....>.



You can change the Drools log level by adding a line to your application's ```grails-app/conf/Config.groovy``` in the appropriate place. For example, to set the level to warn:

```
warn "org.drools"
```

You can change the plugin log level by adding a a line to your application's ```grails-app/conf/Config.groovy``` in the appropriate place. For example, to set the level to debug:

```
debug "org.grails.plugins.drools"
```-->

### Batch Commands
TODO

<!--A <kie:batch> element can be used to define a set of batch commands for a given ksession.This tag has no attributes and must be present directly under a <kie:ksession....> element. The commands supported are

insert-object
	ref = String (optional)
	Anonymous bean
set-global
	identifier = String (required)
	reg = String (optional)
	Anonymous bean
fire-all-rules
	max : n
	fire-until-halt
start-process
	parameter
	identifier = String (required)
	ref = String (optional)
	Anonymous bean
signal-event
	ref = String (optional)
	event-type = String (required)
	process-instance-id =n (optional)

-->