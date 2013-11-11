package grails.plugin.drools

import org.drools.compiler.rule.builder.dialect.java.JavaDialectConfiguration
import org.kie.api.io.ResourceType
import org.kie.internal.KnowledgeBase
import org.kie.internal.KnowledgeBaseFactory
import org.kie.internal.builder.KnowledgeBuilder
import org.kie.internal.builder.KnowledgeBuilderConfiguration
import org.kie.internal.builder.KnowledgeBuilderFactory
import org.kie.internal.io.ResourceFactory
import org.kie.internal.runtime.StatefulKnowledgeSession
import org.springframework.util.Assert

class DroolsService {

	static transactional = false

	/**
	 * Fire rules.
	 *
	 * @param id DroolsRule id
	 * @param facts objects to be put in session
	 */
	def fireFromDatabase(id, facts) {

		def droolsRule = DroolsRule.get(id)
		Assert.notNull droolsRule, "DroolsRule not found for key $id"

		log.debug "Firing rules from DroolsRule $id"

		KnowledgeBuilder builder = createBuilder()
		builder.add ResourceFactory.newReaderResource(new StringReader(droolsRule.value)), ResourceType.DRL

		fire builder, facts
	}

	/**
	 * Fire rules.
	 *
	 * @param ruleFile classpath location of the drl file
	 * @param facts objects to be put in session
	 */
	def fireFromFile(String ruleFile, facts) {

		def stream = getClass().getResourceAsStream(ruleFile)
		Assert.notNull stream, "There is no file [$ruleFile] found in classpath"

		log.debug "Firing rules from file [$ruleFile]"

		KnowledgeBuilder builder = createBuilder()
		builder.add ResourceFactory.newClassPathResource(ruleFile, getClass()), ResourceType.DRL

		fire builder, facts
	}

	protected KnowledgeBuilder createBuilder() {
		Properties props = [(JavaDialectConfiguration.JAVA_COMPILER_PROPERTY): 'JANINO']
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(props, null)
		KnowledgeBuilderFactory.newKnowledgeBuilder(config)
	}

	protected void fire(KnowledgeBuilder builder, facts) {

		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase()
		base.addKnowledgePackages builder.getKnowledgePackages()
		StatefulKnowledgeSession session = base.newStatefulKnowledgeSession()

		for (fact in facts) {
			session.insert fact
		}

		session.fireAllRules()
		session.dispose()
	}
}
