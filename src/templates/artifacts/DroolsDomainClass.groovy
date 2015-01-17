@artifact.package@
class @artifact.name@ {

	String rule
	String description
	String packageName

	static mapping = {
		rule type: 'text'
	}

	static constraints = {
		rule blank: false
		description blank: false
		packageName blank: true
	}
}
