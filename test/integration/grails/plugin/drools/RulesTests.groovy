package grails.plugin.drools

import grails.test.spock.IntegrationSpec

import org.kie.api.runtime.KieSession
import org.kie.api.runtime.StatelessKieSession

class RulesTests extends IntegrationSpec {

	def droolsService
	StatelessKieSession applicationStatelessSession
	KieSession ticketStatefulSession

	void "test applicationStatelessSession bean"() {
		when: "age is over 18 and application is made this year"
		def applicant = new Applicant(name: "A Smith", age: 20)
		def application = new Application(dateApplied: new Date())
		def facts = [applicant, application]
		applicationStatelessSession.execute(facts)
		then:
		application.valid

		when: "age is 17 and application is made this year"
		applicant = new Applicant(name: "B Smith", age: 17)
		application = new Application(dateApplied: new Date())
		facts = [applicant, application]
		applicationStatelessSession.execute(facts)
		then:
		!application.valid

		when: "age is over 18 and application is made last year"
		applicant = new Applicant(name: "C Smith", age: 20)
		application = new Application(dateApplied: new Date(114, 0, 1))
		facts = [applicant, application]
		applicationStatelessSession.execute(facts)
		then:
		!application.valid
	}

	void "test executeFromFile"() {
		when: "age is over 18 and application is made this year"
		def applicant = new Applicant(name: "A Smith", age: 20)
		def application = new Application(dateApplied: new Date())
		droolsService.executeFromFile("rules.application.application.drl", [applicant, application])
		then:
		application.valid

		when: "age is 17 and application is made this year"
		applicant = new Applicant(name: "B Smith", age: 17)
		application = new Application(dateApplied: new Date())
		droolsService.executeFromFile("rules.application.application.drl", [applicant, application])
		then:
		!application.valid

		when: "age is over 18 and application is made last year"
		applicant = new Applicant(name: "C Smith", age: 20)
		application = new Application(dateApplied: new Date(114, 0, 1))
		droolsService.executeFromFile("rules.application.application.drl", [applicant, application])
		then:
		!application.valid
	}

	void "test executeFromDatabase with rule id"() {
		given:
		String drlText = new GroovyClassLoader().getResourceAsStream("rules.application.application.drl").text
		def rule = new DroolsRule(rule: drlText, description: "ticket.drl", packageName: "application").save(flush: true)
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
		def classLoader = new GroovyClassLoader()
		String drlText = classLoader.getResourceAsStream("rules.application.application.drl").text
		new DroolsRule(rule: drlText, description: "application.drl", packageName: "application").save(flush: true)
		drlText = classLoader.getResourceAsStream("rules.ticket.ticket.drl").text
		new DroolsRule(rule: drlText, description: "ticket.drl", packageName: "application").save(flush: true)
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

	void "test ticketStatefulSession bean"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))
		def facts = [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		when:
		for (fact in facts) {
			ticketStatefulSession.insert fact
		}
		ticketStatefulSession.fireAllRules()
		ticketStatefulSession.dispose()

		then:
		"Escalate" == t1.status
		5 == t1.customer.discount
		"Escalate" == t2.status
		0 == t2.customer.discount
		"Pending" == t3.status
		0 == t3.customer.discount
	}

	void "test fireFromFile"() {
		given:
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		droolsService.fireFromFile("rules.ticket.ticket.drl", [t1, t1.customer, t2, t2.customer, t3, t3.customer])

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
		def classLoader = new GroovyClassLoader()
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		String drlText = classLoader.getResourceAsStream("rules.ticket.ticket.drl").text
		def rule = new DroolsRule(rule: drlText, description: "ticket.drl", packageName: "ticket").save(flush: true)
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
		def classLoader = new GroovyClassLoader()
		def t1 = new Ticket(1, new Customer("Jack", "Gold"))
		def t2 = new Ticket(2, new Customer("Tom", "Silver"))
		def t3 = new Ticket(3, new Customer("Bill", "Bronze"))

		when:
		String drlText = classLoader.getResourceAsStream("rules.ticket.ticket.drl").text
		new DroolsRule(rule: drlText, description: "ticket.drl", packageName: "ticket").save(flush: true)
		drlText = classLoader.getResourceAsStream("rules.application.application.drl").text
		new DroolsRule(rule: drlText, description: "application.drl", packageName: "ticket").save(flush: true)
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
