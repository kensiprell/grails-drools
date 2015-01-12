import grails.util.Environment
import groovy.xml.MarkupBuilder

configurationType = grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"  // "droolsContextXml
drlFileLocation = grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "src/drools" // user determined

// TODO must run test-app twice in order to get *.drl on classpath

eventCompileStart = {
	if (isPluginProject) {
		def dir = "${basedir}/test/integration/grails/plugin/drools"
		projectCompiler.srcDirectories << dir
		ant.copy(todir: buildSettings.resourcesDir,
				failonerror: false,
				preservelastmodified: true) {
			fileset(dir: dir) {
				include(name: '*.drl')
			}
		}
	} else {
		projectCompiler.srcDirectories << drlFileLocation
		copyResources buildSettings.resourcesDir
	}
}

eventCompileEnd = {
	if (configurationType == "droolsConfigGroovy") {
		writeDroolsContentXml(basedir, isPluginProject)
	}
}

eventCreateWarStart = { warName, stagingDir ->
	copyResources "$stagingDir/WEB-INF/classes"
}

private copyResources(destination) {
	ant.copy(todir: destination,
			failonerror: false,
			preservelastmodified: true) {
		fileset(dir: drlFileLocation) {
			exclude(name: '*.groovy')
			exclude(name: '*.java')
		}
	}
}

private writeDroolsContentXml(basedir, isPluginProject) {
	def droolsConfigFile
	def droolsContextXmlFile
	def slurper = new ConfigSlurper(Environment.current.name)
	if (isPluginProject) {
		droolsConfigFile = new File("${droolsPluginDir}/grails-app/conf/DroolsDefaultConfig.groovy").toURI().toURL()
		droolsContextXmlFile = new File("$droolsPluginDir/src/templates/drools-default-context.xml")
	} else {
		droolsConfigFile = new File("${basedir}/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
		droolsContextXmlFile = new File("${basedir}/grails-app/conf/drools-context.xml")
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
	}
	def writer = new StringWriter()
	def droolsContentXml = new MarkupBuilder(writer)
	droolsContentXml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
	droolsContentXml.beans(xmlns: "http://www.springframework.org/schema/beans", "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance", "xmlns:kie": "http://drools.org/schema/kie-spring", "xsi:schemaLocation": "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://drools.org/schema/kie-spring http://drools.org/schema/kie-spring.xsd") {
		"kie:kmodule"(id: "kieModule") {
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
	}
	droolsContextXmlFile.write writer.toString()
	if (isPluginProject) {
		new File("$droolsPluginDir/grails-app/conf/drools-default-context.xml").write writer.toString()
	}
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