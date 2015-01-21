package grails.plugin.drools

import org.kie.api.KieBase
import org.kie.api.KieServices
import org.kie.api.builder.KieBuilder
import org.kie.api.builder.KieFileSystem
import org.kie.api.builder.Message
import org.kie.api.builder.Results
import org.kie.api.io.Resource
import org.kie.api.runtime.KieContainer
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.StatelessKieSession

class DroolsService {

	static transactional = false

	def grailsApplication

	/**
	 * Execute rules retrieved from a single database record using a stateless session
	 * @param id the database rule to use
	 * @param facts objects to be inserted in the session
	 */
	def executeFromDatabase(Long id, List facts) {
		String rule = getDatabaseRule(id)
		if (!rule) {
			return
		}

		Resource resource = KieServices.Factory.get().resources.newByteArrayResource(rule.bytes)
		execute(resource, facts)
	}

	/**
	 * Execute rules retrieved from a database based on the packageName property using a stateless session
	 * @param packageName the database rules to use
	 * @param facts objects to be inserted in the session
	 */
	def executeFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		if (!rules) {
			return
		}

		Resource resource = KieServices.Factory.get().resources.newByteArrayResource(rules)
		if (resource){
			fire(resource, facts)
		} else {
			log.error("Kie Resource is null")
		}
	}

	/**
	 * Execute rules retrieved from a single rule file (classpath) using a stateless session
	 * @param file the rule file path to use
	 * @param facts objects to be inserted in the session
	 */
	def executeFromFile(String file, List facts) {
		Resource resource = KieServices.Factory.get().resources.newClassPathResource(file)
		execute(resource, facts)
	}

	/**
	 * Fire rules retrieved from a single database record using a stateful session
	 * @param id the database rule to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromDatabase(Long id, List facts) {
		String rule = getDatabaseRule(id)
		if (!rule) {
			return
		}

		Resource resource = KieServices.Factory.get().resources.newByteArrayResource(rule.bytes)
		fire(resource, facts)
	}

	/**
	 * Fire rules retrieved from a database based on the packageName property using a stateful session
	 * @param packageName the database rules to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		if (!rules) {
			return
		}

		Resource resource = KieServices.Factory.get().resources.newByteArrayResource(rules)
		fire(resource, facts)
	}

	/**
	 * Fire rules retrieved from a single rule file (classpath) using a stateful session
	 * @param file the rule file path to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromFile(String file, List facts) {
		Resource resource = KieServices.Factory.get().resources.newClassPathResource(file)
		fire(resource, facts)
	}

	protected getDatabaseRule(Long id) {
		droolsRuleDomainClass?.get(id)?.rule
	}

	protected getDatabaseRules(String packageName) {
		def domainClass = droolsRuleDomainClass
		if (!domainClass) {
			return
		}

		def rules = new StringBuilder()
		domainClass.findAllByPackageName(packageName).each { rules << it.rule << ' ' }
		rules.toString().bytes
	}

	protected execute(Resource resource, List facts) {
		if (!resource) {
			log.error("Kie Resource is null")
			return
		}

		KieBase kieBase = buildKieBase(resource)
		StatelessKieSession kieSession = kieBase.newStatelessKieSession()
		kieSession.execute(facts)
	}

	protected fire(Resource resource, List facts) {
		if (!resource) {
			log.error("Kie Resource is null")
			return
		}

		KieBase kieBase = buildKieBase(resource)
		KieSession kieSession = kieBase.newKieSession()
		for (fact in facts) {
			kieSession.insert fact
		}
		kieSession.fireAllRules()
		kieSession.dispose()
	}

	protected KieBase buildKieBase(Resource resource) {
		KieServices kieServices = KieServices.Factory.get()
		KieFileSystem kfs = kieServices.newKieFileSystem()
		kfs.write("src/main/resources/rule.drl", resource)
		KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll()
		Results results = kieBuilder.results
		if (results.hasMessages(Message.Level.ERROR)) {
			throw new IllegalStateException(results.messages.toString())
		}
		KieContainer kieContainer = kieServices.newKieContainer(kieServices.repository.defaultReleaseId)
		kieContainer.kieBase
	}

	protected Class getDroolsRuleDomainClass() {
		String className = grailsApplication.config.grails.plugin.drools.droolsRuleDomainClass
		if (!className) {
			log.error("You must set grails.plugin.drools.droolsRuleDomainClass in Config.groovy")
			return null
		}
		grailsApplication.getDomainClass(className).clazz
	}
}
