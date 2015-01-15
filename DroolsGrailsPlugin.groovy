class DroolsGrailsPlugin {

	String version = '0.4.1'
	String grailsVersion = '2.0 > *'
	List pluginExcludes = [
		'docs/**',
		'src/docs/**',
		'grails-app/conf/drools-context.xml',
		'grails-app/conf/DroolsTestConfig.groovy',
		'grails-app/domain',
		'src/rules/**'
	]

	String title = 'Drools Plugin'
	String description = 'Integrates Drools'
	String documentation = 'http://grails.org/plugin/drools'

	String license = 'APACHE'
	def developers = [
		[name: 'Burt Beckwith', email: 'burt@burtbeckwith.com']
	]
	def issueManagement = [system: 'JIRA', url: 'https://github.com/burtbeckwith/grails-drools/issues']
	def scm = [url: 'https://github.com/burtbeckwith/grails-drools']

	// TODO scripts
	//create-drools-config
	//create-drools-context
	//create-drools-domain

	def doWithSpring = {
		importBeans("drools-context.xml")
	}
}

