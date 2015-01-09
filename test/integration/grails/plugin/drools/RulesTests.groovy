package grails.plugin.drools

import spock.lang.Specification

class RulesTests extends Specification {

	def droolsService

	void "testFromDrl"() {
		given:
		def t1 = new Ticket(1, new Customer('Jack', 'Gold'))
		def t2 = new Ticket(2, new Customer('Tom',  'Silver'))
		def t3 = new Ticket(3, new Customer('Bill', 'Bronze'))

		when:
		droolsService.fireFromFile 'ticket_example.drl', [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		then:
		'Escalate' == t1.status
		5 == t1.customer.discount
		'Escalate' == t2.status
		0 == t2.customer.discount
		'Pending' == t3.status
		0 == t3.customer.discount
	}

	void "testFromDatabase"() {
		given:
		def t1 = new Ticket(1, new Customer('Jack', 'Gold'))
		def t2 = new Ticket(2, new Customer('Tom',  'Silver'))
		def t3 = new Ticket(3, new Customer('Bill', 'Bronze'))

		when:
		String drlText = getClass().getResourceAsStream('ticket_example.drl').text
		def rule = new DroolsRule(value: drlText, description: 'ticket_example.drl').save(flush: true)
		DroolsRule.withSession { it.clear() }
		droolsService.fireFromDatabase rule.id, [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		then:
		'Escalate' == t1.status
		5 == t1.customer.discount
		'Escalate' == t2.status
		0 == t2.customer.discount
		'Pending' == t3.status
		0 == t3.customer.discount
	}
}