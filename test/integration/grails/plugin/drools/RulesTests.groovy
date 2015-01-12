package grails.plugin.drools

import spock.lang.Specification

class RulesTests extends Specification {

	def droolsService
	def kieModule
	def kieBase
	def kieSessionStateful
	def kieSessionStateless

	void "test drools-default-context.xml beans"() {
		given:
		kieModule =~ /KieModuleModel/
		kieBase =~ /KnowledgeBaseImpl/
		kieSessionStateful =~ /StatefulKnowledgeSessionImpl/
		kieSessionStateless =~ /StatelessKnowledgeSessionImpl/
	}

	void "test kieSessionStateful"() {
		// TODO
	}

	void "test kieSessionStateless"() {
		// TODO
	}

	void "test no_kie_base.drl from file"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom",  "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		droolsService.fireFromFile "no_kie_base.drl", [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}

	void "test no_kie_base.drl from database"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom",  "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		String drlText = getClass().getResourceAsStream("no_kie_base.drl").text
		def rule = new DroolsRule(value: drlText, description: "no_kie_base.drl").save(flush: true)
		DroolsRule.withSession { it.clear() }
		droolsService.fireFromDatabase rule.id, [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}
}