package grails.plugin.drools

import static junit.framework.Assert.*

class RulesTests {

	def droolsService

	void testFromDrl() {
		def t1 = new Ticket(1, new Customer('Jack', 'Gold'))
		def t2 = new Ticket(2, new Customer('Tom',  'Silver'))
		def t3 = new Ticket(3, new Customer('Bill', 'Bronze'))

		droolsService.fireFromFile 'ticket_example.drl', [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		assertEquals 'Escalate', t1.status
		assertEquals 5, t1.customer.discount

		assertEquals 'Escalate', t2.status
		assertEquals 0, t2.customer.discount

		assertEquals 'Pending', t3.status
		assertEquals 0, t3.customer.discount
	}

	void testFromDatabase() {
		def t1 = new Ticket(1, new Customer('Jack', 'Gold'))
		def t2 = new Ticket(2, new Customer('Tom',  'Silver'))
		def t3 = new Ticket(3, new Customer('Bill', 'Bronze'))

		String drlText = getClass().getResourceAsStream('ticket_example.drl').text
		def rule = new DroolsRule(value: drlText, description: 'ticket_example.drl').save(flush: true)

		DroolsRule.withSession { it.clear() }

		droolsService.fireFromDatabase rule.id, [t1, t1.customer, t2, t2.customer, t3, t3.customer]

		assertEquals 'Escalate', t1.status
		assertEquals 5, t1.customer.discount

		assertEquals 'Escalate', t2.status
		assertEquals 0, t2.customer.discount

		assertEquals 'Pending', t3.status
		assertEquals 0, t3.customer.discount
	}
}
