@artifact.package@class @artifact.name@ {

	String ruleText    // Do not change or delete this field.
	String packageName // Do not change or delete this field.
	String description

	static mapping = {
		ruleText type: 'text'
	}

	static constraints = {
		ruleText blank: false
		packageName blank: true
		description blank: false
	}
}
