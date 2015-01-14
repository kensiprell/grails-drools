package grails.plugin.drools

import org.kie.api.io.Resource
import org.kie.api.KieBase
import org.kie.api.KieServices
import org.kie.api.builder.KieBuilder
import org.kie.api.builder.KieFileSystem
import org.kie.api.builder.Message
import org.kie.api.builder.Results
import org.kie.api.runtime.KieContainer
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.StatelessKieSession
import org.springframework.util.Assert

class DroolsService {

	static transactional = false
	def grailsApplication

	def executeFromDatabase(Long id, List facts) {
		def droolsRule = DroolsRule.get(id)
		Assert.notNull droolsRule, "DroolsRule not found for key $id"

		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newByteArrayResource(droolsRule.value.bytes)
		execute(resource, facts)
	}

	def executeFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newByteArrayResource(rules)
		fire(resource, facts)
	}

	def executeFromFile(String file, List facts) {
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newClassPathResource(file)
		execute(resource, facts)
	}

	def fireFromDatabase(Long id, List facts) {
		def droolsRule = DroolsRule.get(id)
		Assert.notNull droolsRule, "DroolsRule not found for key $id"

		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newByteArrayResource(droolsRule.value.bytes)
		fire(resource, facts)
	}

	def fireFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newByteArrayResource(rules)
		fire(resource, facts)
	}

	def fireFromFile(String file, List facts) {
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newClassPathResource(file)
		fire(resource, facts)
	}

	protected getDatabaseRules(String packageName) {
		String className = grailsApplication.config.grails.plugin.drools.domainClass
		Class clazz = grailsApplication.getDomainClass(className).clazz
		def rules = ""
		clazz.findAllByPackageName(packageName).each {
			rules += "$it.value "
		}
		rules.bytes
	}

	protected static execute(Resource resource, List facts) {
		Object[] factsObj = facts
		KieBase kieBase = buildKieBase(resource)
		StatelessKieSession kieSession = kieBase.newStatelessKieSession()
		kieSession.execute(Arrays.asList(factsObj))
	}

	protected static fire(Resource resource, List facts) {
		KieBase kieBase = buildKieBase(resource)
		KieSession kieSession = kieBase.newKieSession()
		for (fact in facts) {
			kieSession.insert fact
		}
		kieSession.fireAllRules()
		kieSession.dispose()
	}

	protected static KieBase buildKieBase(Resource resource) {
		KieServices kieServices = KieServices.Factory.get()
		KieFileSystem kfs = kieServices.newKieFileSystem()
		kfs.write("src/main/resources/rule.drl", resource)
		KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll()
		Results results = kieBuilder.results
		if (results.hasMessages(Message.Level.ERROR)) {
			//println results.messages
			throw new IllegalStateException(results.messages.toString())
		}
		KieContainer kieContainer = kieServices.newKieContainer(kieServices.repository.defaultReleaseId)
		kieContainer.kieBase
	}

}
