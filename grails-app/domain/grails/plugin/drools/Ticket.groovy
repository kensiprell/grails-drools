package grails.plugin.drools

class Ticket {
	int id
	Customer customer
	String status

	Ticket(Integer id, Customer customer) {
		this.id = id
		this.customer = customer
		status = 'New'
	}

	String toString() {
		"Ticket #$id: Customer[$customer] Status[$status]"
	}
}
