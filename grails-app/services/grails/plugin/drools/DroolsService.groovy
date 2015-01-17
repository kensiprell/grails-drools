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
	// TODO log.error strings

	static transactional = false
	def grailsApplication

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

	def executeFromFile(String file, List facts) {
		KieServices kieServices = KieServices.Factory.get()
		Resource resource = kieServices.resources.newClassPathResource(file)
		if (resource) {
			execute(resource, facts)
		} else {
			log.error("Kie Resource is null")
		}
	}

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
		String className = grailsApplication.config.grails.plugin.drools.domainClass
		if (!className) {
			log.error("You must set grails.plugin.drools.domainClass in Config.groovy")
			return
		}
		Class clazz = grailsApplication.getDomainClass(className).clazz
		clazz.get(id).rule
	}

	protected getDatabaseRules(String packageName) {
		String className = grailsApplication.config.grails.plugin.drools.domainClass
		if (!className) {
			log.error("You must set grails.plugin.drools.domainClass in Config.groovy")
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
