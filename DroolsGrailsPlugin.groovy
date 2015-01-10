import org.kie.spring.KModuleBeanFactoryPostProcessor

class DroolsGrailsPlugin {

	String version = '0.4.1'
	String grailsVersion = '2.0 > *'
	List pluginExcludes = [
		'docs/**',
		'src/docs/**',
		'grails-app/drools'
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
		importBeans("drools-default-context.xml")

		kiePostProcessor(KModuleBeanFactoryPostProcessor) { bean ->
		}
	}
}

