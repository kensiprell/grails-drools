package grails.plugin.drools

class Customer {
	String name
	String subscription
	int discount

	Customer(String name, String subscription) {
		this.name = name
		this.subscription = subscription
	}

	String toString() {
		"Name: '$name', Subscription: '$subscription', Discount: $discount %"
	}
}
