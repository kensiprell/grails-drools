package grails.plugin.drools

import org.kie.api.KieBase
import org.kie.api.builder.model.KieModuleModel
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.StatelessKieSession
import spock.lang.Specification

class RulesTests extends Specification {

	def droolsService
	KieModuleModel defaultKieModule
	KieBase defaultKieBase
	KieSession defaultKieStatefulSession  // StatefulKnowledgeSessionImpl
	StatelessKieSession defaultKieStatelessSession    // StatelessKnowledgeSessionImpl
	def kiePostProcessor

/*
	void "test drools-default-context.xml beans"() {
		given:
		println defaultKieModule.getKieBaseModels()  //.toXML() //.kieBaseModels.size() //!= null
		println defaultKieBase.kiePackages //.kieSessions.size() //!= null
		println defaultKieStatefulSession //!= null
		println defaultKieStatelessSession //!= null
		kiePostProcessor != null
	}
*/

	void "test executeFromFile"() {
		when: "age is over 18 and application is made this year"
		def applicant = new Applicant(name: "A Smith", age: 20)
		def application = new Application(dateApplied: new Date())
		droolsService.executeFromFile("application.drl", [applicant, application])
		then:
		application.valid

		when: "age is 17 and application is made this year"
		applicant = new Applicant(name: "B Smith", age: 17)
		application = new Application(dateApplied: new Date())
		droolsService.executeFromFile("application.drl", [applicant, application])
		then:
		!application.valid

		when: "age is over 18 and application is made last year"
		applicant = new Applicant(name: "C Smith", age: 20)
		application = new Application(dateApplied: new Date(114, 0, 1))
		droolsService.executeFromFile("application.drl", [applicant, application])
		then:
		!application.valid
	}

	void "test executeFromDatabase with rule id"() {
		given:
		String drlText = getClass().getResourceAsStream("application.drl").text
		def rule = new DroolsRule(value: drlText, description: "ticket.drl", packageName: "application").save(flush: true)
		DroolsRule.withSession { it.clear() }

		when: "age is over 18 and application is made this year"
		def applicant = new Applicant(name: "A Smith", age: 20)
		def application = new Application(dateApplied: new Date())
		droolsService.executeFromDatabase(rule.id, [applicant, application])
		then:
		application.valid

		when: "age is 17 and application is made this year"
		applicant = new Applicant(name: "B Smith", age: 17)
		application = new Application(dateApplied: new Date())
		droolsService.executeFromDatabase(rule.id, [applicant, application])
		then:
		!application.valid

		when: "age is over 18 and application is made last year"
		applicant = new Applicant(name: "C Smith", age: 20)
		application = new Application(dateApplied: new Date(114, 0, 1))
		droolsService.executeFromDatabase(rule.id, [applicant, application])
		then:
		!application.valid
	}

	void "test executeFromDatabase with packageName"() {
		given:
		String drlText = getClass().getResourceAsStream("db_application.drl").text
		new DroolsRule(value: drlText, description: "db_application.drl", packageName: "application").save(flush: true)
		drlText = getClass().getResourceAsStream("db_ticket.drl").text
		new DroolsRule(value: drlText, description: "db_ticket.drl", packageName: "application").save(flush: true)
		DroolsRule.withSession { it.clear() }

		when: "age is over 18 and application is made this year"
		def applicant = new Applicant(name: "A Smith", age: 20)
		def application = new Application(dateApplied: new Date())
		droolsService.executeFromDatabase("application", [applicant, application])
		then:
		application.valid

		when: "age is 17 and application is made this year"
		applicant = new Applicant(name: "B Smith", age: 17)
		application = new Application(dateApplied: new Date())
		droolsService.executeFromDatabase("application", [applicant, application])
		then:
		!application.valid

		when: "age is over 18 and application is made last year"
		applicant = new Applicant(name: "C Smith", age: 20)
		application = new Application(dateApplied: new Date(114, 0, 1))
		droolsService.executeFromDatabase("application", [applicant, application])
		then:
		!application.valid
	}

	void "test fireFromFile"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		droolsService.fireFromFile("ticket.drl", [t1, t1.customer, t2, t2.customer, t3, t3.customer])

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}

	void "test fireFromDatabase with rule id"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		String drlText = getClass().getResourceAsStream("ticket.drl").text
		def rule = new DroolsRule(value: drlText, description: "ticket.drl", packageName: "ticket").save(flush: true)
		DroolsRule.withSession { it.clear() }
		droolsService.fireFromDatabase(rule.id, [t1, t1.customer, t2, t2.customer, t3, t3.customer])

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}

	void "test fireFromDatabase with packageName"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		String drlText = getClass().getResourceAsStream("db_ticket.drl").text
		new DroolsRule(value: drlText, description: "db_ticket.drl", packageName: "ticket").save(flush: true)
		drlText = getClass().getResourceAsStream("db_application.drl").text
		new DroolsRule(value: drlText, description: "db_application.drl", packageName: "ticket").save(flush: true)
		DroolsRule.withSession { it.clear() }
		droolsService.fireFromDatabase("ticket", [t1, t1.customer, t2, t2.customer, t3, t3.customer])

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}
}