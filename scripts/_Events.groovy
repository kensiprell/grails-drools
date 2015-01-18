import static groovy.io.FileType.FILES
import grails.util.Environment
import groovy.xml.MarkupBuilder

includeTargets << new File("${droolsPluginDir}/scripts/_DroolsUtils.groovy")

configurationType = grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"
drlFileLocation = grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "src/rules"
sourceDir = new File("${basedir}/${drlFileLocation}")

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

private copyFiles(destination) {
	sourceDir.traverse(type: FILES) {
		def newName = "rules$it.path" - "$basedir/$drlFileLocation"
		newName = newName.replaceAll("/", ".")
		newFile = new File("${destination}/$newName")
		newFile.write("$it.text")
	}
}

private writeDroolsContentXml(basedir, isPluginProject) {
	def droolsConfigFile
	def droolsContextXmlFile = new File("${basedir}/grails-app/conf/drools-context.xml")
	def slurper = new ConfigSlurper(Environment.current.name)
	if (isPluginProject) {
		droolsConfigFile = new File("${droolsPluginDir}/grails-app/conf/DroolsTestConfig.groovy").toURI().toURL()
	} else {
		droolsConfigFile = new File("${basedir}/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
		if (!droolsConfigFile) {
			copyDroolsConfig()
			droolsConfigFile = new File("${basedir}/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
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
	droolsContentXml.beans(xmlns: "http://www.springframework.org/schema/beans", "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance", "xmlns:kie": "http://drools.org/schema/kie-spring", "xsi:schemaLocation": "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://drools.org/schema/kie-spring http://drools.org/schema/kie-spring.xsd") {
		"kie:kmodule"(id: "defaultKieModule") {
			droolsConfig.kieBases.each { kieBase ->
				if (!kieBase.includeInConfig) return
				"kie:kbase"(kieBase.attributes) {
					kieBase.kieSessions.each { kieSession ->
						if (!kieSession.includeInConfig) return
						"kie:ksession"(kieSession.attributes) {
							kieSession.listeners.each { listener ->
								if (!listener.includeInConfig) return
								"kie:$listener.type"(listener.attributes)
							}
						}
					}
				}
			}
		}
		// TODO iterate over event listeners
		bean(id: "kiePostProcessor", class: "org.kie.spring.KModuleBeanFactoryPostProcessor")
	}
	droolsContextXmlFile.write writer.toString()
}

/*
Reference: http://docs.jboss.org/drools/release/6.1.0.Final/drools-docs/html/ch.kie.spring.html

Example 11.4. Listener configuration example - using a bean:ref.
<bean id="mock-agenda-listener" class="mocks.MockAgendaEventListener"/>
<bean id="mock-rr-listener" class="mocks.MockRuleRuntimeEventListener"/>
<bean id="mock-process-listener" class="mocks.MockProcessEventListener"/>
<kie:kmodule id="listeners_kmodule">
  <kie:kbase name="drl_kiesample" packages="drl_kiesample">
    <kie:ksession name="ksession2">
      <kie:agendaEventListener ref="mock-agenda-listener"/>
      <kie:processEventListener ref="mock-process-listener"/>
      <kie:ruleRuntimeEventListener ref="mock-rr-listener"/>
    </kie:ksession>
  </kie:kbase>
</kie:kmodule>

Example 11.5. Listener configuration example - using nested bean.
<kie:kmodule id="listeners_module">
  <kie:kbase name="drl_kiesample" packages="drl_kiesample">
   <kie:ksession name="ksession1">
      <kie:agendaEventListener>
      <bean class="mocks.MockAgendaEventListener"/>
      </kie:agendaEventListener>
    </kie:ksession>
  </kie:kbase>
</kie:kmodule>

Example 11.6. Listener configuration example - defaulting to the debug versions provided by the Knowledge-API .
<bean id="mock-agenda-listener" class="mocks.MockAgendaEventListener"/>
<bean id="mock-rr-listener" class="mocks.MockRuleRuntimeEventListener"/>
<bean id="mock-process-listener" class="mocks.MockProcessEventListener"/>
<kie:kmodule id="listeners_module">
 <kie:kbase name="drl_kiesample" packages="drl_kiesample">
    <kie:ksession name="ksession2">
      <kie:agendaEventListener />
      <kie:processEventListener />
      <kie:ruleRuntimeEventListener />
    </kie:ksession>
 </kie:kbase>
</kie:kmodule>

Example 11.7. Listener configuration example - mix and match of 'ref'/nested-bean/empty styles.
<bean id="mock-agenda-listener" class="mocks.MockAgendaEventListener"/>
<bean id="mock-rr-listener" class="mocks.MockRuleRuntimeEventListener"/>
<bean id="mock-process-listener" class="mocks.MockProcessEventListener"/>
<kie:kmodule id="listeners_module">
  <kie:kbase name="drl_kiesample" packages="drl_kiesample">
    <kie:ksession name="ksession1">
      <kie:agendaEventListener>
          <bean class="org.kie.spring.mocks.MockAgendaEventListener"/>
      </kie:agendaEventListener>
    </kie:ksession>
    <kie:ksession name="ksession2">
      <kie:agendaEventListener ref="mock-agenda-listener"/>
      <kie:processEventListener ref="mock-process-listener"/>
      <kie:ruleRuntimeEventListener ref="mock-rr-listener"/>
    </kie:ksession>
  </kie:kbase>
</kie:kmodule>

Example 11.8. Listener configuration example - multiple listeners of the same type.
<bean id="mock-agenda-listener" class="mocks.MockAgendaEventListener"/>
<kie:kmodule id="listeners_module">
  <kie:kbase name="drl_kiesample" packages="drl_kiesample">
    <kie:ksession name="ksession1">
      <kie:agendaEventListener ref="mock-agenda-listener"/>
      <kie:agendaEventListener>
          <bean class="org.kie.spring.mocks.MockAgendaEventListener"/>
      </kie:agendaEventListener>
    </kie:ksession>
  </kie:kbase>
</kie:kmodule>

Example 11.9. Group of listeners - example
<bean id="mock-agenda-listener" class="mocks.MockAgendaEventListener"/>
<bean id="mock-rr-listener" class="mocks.MockRuleRuntimeEventListener"/>
<bean id="mock-process-listener" class="mocks.MockProcessEventListener"/>
<kie:kmodule id="listeners_module">
  <kie:kbase name="drl_kiesample" packages="drl_kiesample">
    <kie:ksession name="statelessWithGroupedListeners" type="stateless"
             listeners-ref="debugListeners"/>
  </kie:kbase>
</kie:kmodule>
<kie:eventListeners id="debugListeners">
  <kie:agendaEventListener ref="mock-agenda-listener"/>
  <kie:processEventListener ref="mock-process-listener"/>
  <kie:ruleRuntimeEventListener ref="mock-rr-listener"/>
</kie:eventListeners>
 */