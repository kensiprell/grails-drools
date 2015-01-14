package grails.plugin.drools

class DroolsRule {

	String value
	String description
	String packageName

	static mapping = {
		value type: 'text'
	}

	static constraints = {
		value blank: false
		description blank: false
		packageName blank: true
	}
}
