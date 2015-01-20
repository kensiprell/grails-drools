import static groovy.io.FileType.FILES
import grails.util.Environment
import groovy.xml.MarkupBuilder

import org.kie.spring.KModuleBeanFactoryPostProcessor

includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

configurationType = grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"
drlFileLocation = grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "src/rules"
sourceDir = new File(basedir, drlFileLocation)

eventCompileEnd = {
	if (configurationType == "droolsConfigGroovy") {
		writeDroolsContentXml(basedir, isPluginProject)
	}
	copyFiles(buildSettings.classesDir)
}

eventTestCompileEnd = {
	def integrationPath = "${grailsSettings.testClassesDir}/integration"
	def integrationDir = new File(integrationPath)
	if (integrationDir.exists()) {
		copyFiles(integrationPath)
	}
}

eventCreateWarEnd = { warName, stagingDir ->
	copyFiles("$stagingDir/WEB-INF/classes")
}

private void copyFiles(destination) {
	sourceDir.traverse(type: FILES) {
		String newName = ("rules$it.path" - "$basedir/$drlFileLocation").replaceAll("/", ".")
		def newFile = new File(destination, newName)
		newFile.parentFile.mkdirs()
		newFile.write(it.text)
	}
}

private void writeDroolsContentXml(basedir, isPluginProject) {
	def droolsConfigFile
	def droolsContextXmlFile = new File(basedir, "grails-app/conf/drools-context.xml")
	def slurper = new ConfigSlurper(Environment.current.name)
	if (isPluginProject) {
		droolsConfigFile = new File(droolsPluginDir, "grails-app/conf/DroolsTestConfig.groovy").toURI().toURL()
	} else {
		droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy").toURI().toURL()
		if (!droolsConfigFile) {
			copyDroolsConfig()
			droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy").toURI().toURL()
		}
	}
	try {
		droolsConfig = slurper.parse(droolsConfigFile)
	}
	catch (e) {
		if (isPluginProject) {
			println "ERROR _Events.groovy: $e"
		} else {
			println "ERROR: grails-app/conf/DroolsConfig.groovy does not exist. Run 'grails create-drools-config'."
		}
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
		bean(id: "kiePostProcessor", class: KModuleBeanFactoryPostProcessor.name)
	}
	droolsContextXmlFile.write writer.toString()
}

private listenerTypeCheck(type) {
	["agendaEventListener", "processEventListener", "ruleRuntimeEventListener"].contains(type)
}
