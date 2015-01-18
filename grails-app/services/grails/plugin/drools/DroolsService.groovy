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
		if (rule) {
			KieServices kieServices = KieServices.Factory.get()
			Resource resource = kieServices.resources.newByteArrayResource(rule.bytes)
			if (resource) {
				execute(resource, facts)
			} else {
				log.error("Kie Resource is null")
			}
		}
	}

	/**
	 * Execute rules retrieved from a database based on the packageName property using a stateless session
	 * @param packageName the database rules to use
	 * @param facts objects to be inserted in the session
	 */
	def executeFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		if (rules) {
			KieServices kieServices = KieServices.Factory.get()
			Resource resource = kieServices.resources.newByteArrayResource(rules)
			if (resource){
				fire(resource, facts)
			} else {
				log.error("Kie Resource is null")
			}
		}
	}

	/**
	 * Execute rules retrieved from a single rule file (classpath) using a stateless session
	 * @param file the rule file path to use
	 * @param facts objects to be inserted in the session
	 */
	def executeFromFile(String file, List facts) {
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newClassPathResource(file)
		if (resource) {
			execute(resource, facts)
		} else {
			log.error("Kie Resource is null")
		}
	}

	/**
	 * Fire rules retrieved from a single database record using a stateful session
	 * @param id the database rule to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromDatabase(Long id, List facts) {
		String rule = getDatabaseRule(id)
		if (rule) {
			KieServices kieServices = KieServices.Factory.get()
			Resource resource = kieServices.resources.newByteArrayResource(rule.bytes)
			if (resource) {
				fire(resource, facts)
			} else {
				log.error("Kie Resource is null")
			}
		}
	}

	/**
	 * Fire rules retrieved from a database based on the packageName property using a stateful session
	 * @param packageName the database rules to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromDatabase(String packageName, List facts) {
		def rules = getDatabaseRules(packageName)
		if (rules) {
			KieServices kieServices = KieServices.Factory.get()
			Resource resource = kieServices.resources.newByteArrayResource(rules)
			if (resource) {
				fire(resource, facts)
			} else {
				log.error("Kie Resource is null")
			}
		}
	}

	/**
	 * Fire rules retrieved from a single rule file (classpath) using a stateful session
	 * @param file the rule file path to use
	 * @param facts objects to be inserted in the session
	 */
	def fireFromFile(String file, List facts) {
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newClassPathResource(file)
		if (resource) {
			fire(resource, facts)
		} else {
			log.error("Kie Resource is null")
		}
	}

	protected getDatabaseRule(Long id) {
		String className = grailsApplication.config.grails.plugin.drools.droolsRuleDomainClass
		if (!className) {
			log.error("You must set grails.plugin.drools.droolsRuleDomainClass in Config.groovy")
			return
		}
		Class clazz = grailsApplication.getDomainClass(className).clazz
		clazz.get(id).rule
	}

	protected getDatabaseRules(String packageName) {
		String className = grailsApplication.config.grails.plugin.drools.droolsRuleDomainClass
		if (!className) {
			log.error("You must set grails.plugin.drools.droolsRuleDomainClass in Config.groovy")
			return
		}
		Class clazz = grailsApplication.getDomainClass(className).clazz
		def rules = ""
		clazz.findAllByPackageName(packageName).each {
			rules += "$it.rule "
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
		// TODO add boolean to signature for dispose?
		kieSession.dispose()
	}

	protected static KieBase buildKieBase(Resource resource) {
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
}
