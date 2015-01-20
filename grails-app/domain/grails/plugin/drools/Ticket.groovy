package grails.plugin.drools

class Ticket {
	Customer customer
	String status

	Ticket(Long id, Customer customer) {
		this()
		this.id = id
		this.customer = customer
		status = 'New'
	}

	String toString() {
		"Ticket #$id: Customer[$customer] Status[$status]"
	}
}
