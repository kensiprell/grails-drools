@artifact.package@class @artifact.name@ {

	String rule        // Do not change or delete this field.
	String packageName // Do not change or delete this field.
	String description

	static mapping = {
		rule type: 'text'
	}

	static constraints = {
		rule blank: false
		packageName blank: true
		description blank: false
	}
}
