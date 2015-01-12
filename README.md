# Do not use. I'm traveling and using github as a backup medium.


## Grails plugin for integrating Drools

[Drools](https://www.drools.org) is a Business Rules Management System (BRMS) solution.

The plugin has been tested in the following environment using the [grails-drools-sample](https://github.com/kensiprell/grails-drools-sample) application:

* Drools 6.1.0.Final

* OSX 10.10

* JDK 1.7.0_71

* Grails versions 2.1.5, 2.2.4, 2.3.9, and 2.4.4

* Tomcat 7.0.27 through 7.0.54 (depends on Grails version)

If you have a question, problem, suggestion, or want to report a bug, please submit an [issue](https://github.com/kensiprell/grails-drools/issues?state=open). I will reply as soon as I can.

[Release Notes](https://github.com/kensiprell/grails-drools/wiki/Release-Notes)

Uses [Drools kie-spring integration](http://docs.jboss.org/drools/release/6.1.0.Final/drools-docs/html/ch.kie.spring.html).

## How the Plugin Works

### Database and File Rules
Explain concept

#### Database

#### Files

## Plugin Installation, Configuration, and Use

### Installation
Edit your ```BuildConfig.groovy```:

```
plugins {
    // other plugins
    compile ":drools:0.4"
    // other plugins
}
```

### Configuration
Grails convention over configuration disclaimer.

```BuildConfig.groovy``` options. Defaults are shown below:

```grails.plugin.drools.configurationType = "droolsContextXml"```
```grails.plugin.drools.drlFileLocation = "src/drools"```

#### grails.plugin.drools.configurationType

Explain how it works

#### grails.plugin.drools.drlFileLocation

Put rules (filename.drl) in ```grails.plugin.drools.drlFileLocation```

Everything in grails.plugin.drools.drlFileLocation goes to classpath

### Scripts

#### create-drools-content

#### create-drools-config

### Drools Beans

Each:
kie:kbase
kie:ksession
kie:

### Drools Rules
Can have multiple directories under ```grails.plugin.drools.drlFileLocation```.

Use packages (not tied to file location) to load into kbases.

### Event Listeners
Drools supports adding 3 types of listeners to KieSessions - AgendaListener, WorkingMemoryListener, ProcessEventListener
The kie-spring module allows you to configure these listeners to KieSessions using XML tags. These tags have identical names as the actual listener interfaces i.e., <kie:agendaEventListener....>, <kie:ruleRuntimeEventListener....> and <kie:processEventListener....>.
kie-spring provides features to define the listeners as standalone (individual) listeners and also to define them as a group.


### Logging
TODO

Drools supports adding 2 types of loggers to KieSessions - ConsoleLogger, FileLogger.

The kie-spring module allows you to configure these loggers to KieSessions using XML tags. These tags have identical names as the actual logger interfaces i.e., <kie:consoleLogger....> and <kie:fileLogger....>.



You can change the Drools log level by adding a line to your application's ```grails-app/conf/Config.groovy``` in the appropriate place. For example, to set the level to warn:

```
warn "org.drools"
```

You can change the plugin log level by adding a a line to your application's ```grails-app/conf/Config.groovy``` in the appropriate place. For example, to set the level to debug:

```
debug "org.grails.plugins.drools"
```

### Batch Commands
TODO

A <kie:batch> element can be used to define a set of batch commands for a given ksession.This tag has no attributes and must be present directly under a <kie:ksession....> element. The commands supported are

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

