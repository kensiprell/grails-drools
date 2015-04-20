## Grails plugin for integrating Drools

[Drools](https://www.drools.org) is a Business Rules Management System (BRMS) solution. The plugin fully supports Drools [kie-spring](https://docs.jboss.org/drools/release/6.2.0.Final/drools-docs/html/ch11.html) integration.

The plugin has been tested  using the [sample application](https://github.com/kensiprell/grails-drools-sample) and [test script](https://github.com/kensiprell/grails-plugin-test-script/blob/master/drools.sh) in the following environment:

* Drools 6.2.0.Final

* Grails versions 2.3.11 and 2.4.5

* OSX 10.10.3

* JDK 1.7.0_75

If you want to use the plugin with a version of Grails earlier than 2.3.0, see [Grails 2.2.5 and Earlier](https://github.com/kensiprell/grails-drools/wiki/Grails-2.2.5-and-Earlier).

If you have a question, problem, suggestion, or want to report a bug, please submit an [issue](https://github.com/kensiprell/grails-drools/issues). I will reply as soon as I can.

[Release Notes](https://github.com/kensiprell/grails-drools/wiki/Release-Notes)

## Breaking Changes
If you are upgrading from version 0.9.3, there are some breaking changes you will have to address. 
### Rule File Directory
Rule files and subdirectories must be located under `src/resources/`. You can no longer use some arbitrary file system path. This change and the one discussed below were required to align the plugin with Drools conventions.

### drlFileLocation 
`grails.plugin.drools.drlFileLocation` must be a single directory name and not a path. For example, the default of "rules" refers to the following file system path: `src/resources/rules/`.

### Classpath Access
Rule files are now available on the classpath using `/` instead of `.`. Older versions of the plugin violated the DRY principle by having the same rule file in two different locations. For example,

    droolsService.executeFromFile("rules.ruleOne.drl", [facts])

becomes

    droolsService.executeFromFile("rules/ruleOne.drl", [facts])

And

    classLoader.getResourceAsStream("rules.ruleOne.drl")

becomes

    classLoader.getResourceAsStream("rules/ruleOne.drl")


### Rule File Names
Rule files must have a "drl" or "rule" suffix. This change avoids unnecessary clutter in the `target` directory by omitting hidden files and directories.

## How the Plugin Works
The plugin offers a variety of ways to use rules. The [RuleTests](https://github.com/kensiprell/grails-drools/blob/master/test/integration/grails/plugin/drools/RulesTests.groovy) and [TestController](https://github.com/kensiprell/grails-drools-sample/blob/master/grails-app/controllers/grails/plugin/drools_sample/TestController.groovy) classes show several examples.

### Beans
You can define beans using either a configuration file `grails-app/conf/DroolsConfig.groovy` or an xml file `grails-app/conf/drools-context.xml`. This will allow you to do something like this:

    class SomeService {
       def packageOneStatelessSession

       def someMethod() {
          def fact1 = SomeDomain.get(123)
          def fact2 = SomeOtherDomain.get(123)
          def facts = [fact1, fact2]
          packageOneStatelessSession.execute(facts)
       }
    }

### Database and File Rules
The [DroolsService](https://github.com/kensiprell/grails-drools/blob/master/grails-app/services/grails/plugin/drools/DroolsService.groovy) offers several methods to use rules stored in a database or on the file system (classpath). For example, assuming you have several rules in the database with a `packageName` of "ticket", you could do something like this:

    class SomeOtherService {
       def droolsService

       def someMethod() {
          def fact1 = SomeDomain.get(123)
          def fact2 = SomeOtherDomain.get(123)
          droolsService.fireFromDatabase("ticket", [fact1, fact2])
       }
    }

## Plugin Installation and Configuration

### Installation
Edit your `BuildConfig.groovy`:

    plugins {
       // other plugins
       compile ":drools:1.0.1"
    }

### Configuration
Although Grails prefers convention over configuration, you cannot avoid some configuration for the Drools beans.

After the plugin is installed you will find a heavily commented [DroolsConfig.groovy](https://github.com/kensiprell/grails-drools/blob/master/src/templates/conf/DroolsConfig.groovy) in `grails-app/conf/` that you can use as a starting point for configuring your beans. When your application is compiled this file is parsed and `grails-app/conf/drools-context.xml` is created (or overwritten). For example, [DroolsTestConfig.groovy](https://github.com/kensiprell/grails-drools/blob/master/grails-app/conf/DroolsTestConfig.groovy) is the one used for plugin integration tests.

#### BuildConfig.groovy

There are two options that you can configure in your `grails-app/conf/BuildConfig.groovy`. The defaults are shown below:

    grails.plugin.drools.configurationType = "droolsConfigGroovy"
    grails.plugin.drools.drlFileLocation = "rules"

##### grails.plugin.drools.configurationType
The option below will stop the plugin from overwriting `grails-app/conf/drools-context.xml`. This will allow you to edit the file manually without losing changes.

    grails.plugin.drools.configurationType = "droolsContextXml"

##### grails.plugin.drools.drlFileLocation
This option is the directory root for Rule files, those files with a "drl" or "rule" suffix. Note the lack of leading and trailing slashes below:

    grails.plugin.drools.drlFileLocation = "drools-rules"

You can take advantage of rule packages by creating subdirectories under `drlFileLocation`. See the plugin's [src/resources/rules](https://github.com/kensiprell/grails-drools/tree/master/src/resources/rules) for an example.

All files in this directory and its subdirectories with a "drl" or "rule" suffix will be copied to the classpath.

#### Config.groovy
If you change the domain class used to store your rules without using the script `create-drools-domain`, you will have to edit the corresponding configuration option in your `grails-app/conf/Config.groovy`.

    grails.plugin.drools.droolsRuleDomainClass = "com.example.DroolsRule"

#### DroolsConfig.groovy
See [DroolsConfig.groovy](https://github.com/kensiprell/grails-drools/blob/master/src/templates/conf/DroolsConfig.groovy) for configuration options and instructions. [Drools Spring Integration](https://docs.jboss.org/drools/release/6.2.0.Final/drools-docs/html/ch11.html) provides more information.

## Using the Plugin

### Drools Rule Files

Changes to rule files will not be available until the applicaiton is restarted.

Changing the `grails.plugin.drools.drlFileLocation` option could affect the `packages` property for a `KieBase`. For example, for the option

    grails.plugin.drools.drlFileLocation = "drools-rules"

with a rule file located in a subdirectory:

    ~/my-grails-app/src/resources/drools-rules/packageOne/ruleFile1.drl

The rule file will be avaiable on the classpath as

    drools-rules/packageOne/ruleFile1.drl

and the KieBase packages property would be:

    packages: "drools-rules.packageOne"

### Scripts
The plugin offers three command-line scripts.

#### create-drools-config
Running `grails create-drools-config` will copy the default [DroolsConfig.groovy](https://github.com/kensiprell/grails-drools/blob/master/src/templates/conf/DroolsConfig.groovy) to your application's `grails-app/conf` directory.

#### create-drools-context
Running `grails create-drools-context` will copy the default [drools-context.xml](https://github.com/kensiprell/grails-drools/blob/master/src/templates/conf/drools-context.xml)  to your application's `grails-app/conf` directory.

#### create-drools-domain
Running `grails create-drools-config` will create a domain class with a package and name of your choice. Use this class to store your rules in the database.

It will also add or update the configuration option below in your application's `grails-app/conf/Config.groovy`:

    grails.plugin.drools.droolsRuleDomainClass = "com.example.DroolsRule"
