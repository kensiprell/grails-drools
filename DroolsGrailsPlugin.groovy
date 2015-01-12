import org.kie.spring.KModuleBeanFactoryPostProcessor

class DroolsGrailsPlugin {

	String version = '0.4.1'
	String grailsVersion = '2.0 > *'
	List pluginExcludes = [
		'docs/**',
		'src/docs/**',
		'grails-app/conf/drools-default-context.xml'
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

	def doWithSpring = {
		// TODO if plugin; won't work if file is in templates dir
		// TODO use try catch and comment for plugin test
		importBeans("drools-default-context.xml")
		// TODO if app
		//importBeans("drools-context.xml")

		// TODO iterate over config.eventListeners

		kiePostProcessor(KModuleBeanFactoryPostProcessor) { bean ->
		}
	}
}

