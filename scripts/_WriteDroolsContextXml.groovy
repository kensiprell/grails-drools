import grails.util.Environment
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

def pluginDir = GrailsPluginUtils.getPluginDirForName("drools")

private listenerTypeCheck(type) {
	["agendaEventListener", "processEventListener", "ruleRuntimeEventListener"].contains(type)
}

// Create grails-app/conf/drools-context.xml")
def appDir = System.getProperty("user.dir")
def droolsConfigFile
def droolsContextXmlFile = new File(appDir, "grails-app/conf/drools-context.xml")
def slurper = new ConfigSlurper(Environment.current.name)
droolsConfigFile = new File(appDir, "grails-app/conf/DroolsConfig.groovy")
if (!droolsConfigFile.exists()) {
	def shell = new GroovyShell()
	shell.run(new File("$pluginDir/scripts/_CreateDroolsConfig.groovy"))
}
try {
	droolsConfig = slurper.parse(droolsConfigFile.toURI().toURL())
}
catch (e) {
	println "ERROR: grails-app/conf/DroolsConfig.groovy does not exist. Run 'grails create-drools-config'."
	return
}
def writer = new StringWriter()
def droolsContentXml = new MarkupBuilder(writer)
droolsContentXml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
droolsContentXml.beans(xmlns: "http://www.springframework.org/schema/beans",
	"xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
	"xmlns:kie": "http://drools.org/schema/kie-spring",
	"xsi:schemaLocation": "http://www.springframework.org/schema/beans " +
		"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd " +
		"http://drools.org/schema/kie-spring http://drools.org/schema/kie-spring.xsd") {
	"kie:kmodule"(id: "defaultKieModule") {
		droolsConfig.kieBases.each { kieBase ->
			if (!kieBase.includeInConfig) return
			"kie:kbase"(kieBase.attributes) {
				kieBase.kieSessions.each { kieSession ->
					if (!kieSession.includeInConfig) return
					"kie:ksession"(kieSession.attributes) {
						kieSession.kieListeners.each { kieListener ->
							if (!kieListener.includeInConfig) return
							if (!listenerTypeCheck(kieListener.type)) return
							if (kieListener.debug) {
								"kie:$kieListener.type"()
							}
							if (!kieListener.debug && kieListener.ref && !kieListener.nestedBeanClass) {
								"kie:$kieListener.type"(ref: kieListener.ref)
							}
							if (!kieListener.debug && !kieListener.ref && kieListener.nestedBeanClass) {
								"kie:$kieListener.type"() {
									bean(class: kieListener.nestedBeanClass)
								}
							}
						}
					}
				}
			}
		}
	}
	droolsConfig.kieEventListeners.each { listener ->
		if (!listener.includeInConfig) return
		bean(listener.attributes)
	}
	droolsConfig.kieEventListenerGroups.each { group ->
		if (!group.includeInConfig) return
		"kie:eventListeners"(id: group.id) {
			group.listeners.each { listener ->
				if (!listener.includeInConfig) return
				if (!listenerTypeCheck(listener.type)) return
				"kie:$listener.type"(ref: listener.ref)
			}
		}
	}
}
droolsContextXmlFile.write writer.toString()
