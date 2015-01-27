import grails.plugin.drools.DroolsDomainClassArtefactHandler

class DroolsGrailsPlugin {

	def version = "0.9.2"
	def grailsVersion = "2.2 > *"
	def pluginExcludes = [
		"grails-app/conf/drools-context.xml",
		"grails-app/conf/DroolsTestConfig.groovy",
		"grails-app/domain/**",
		"src/rules/**"
	]
	def title = "Drools Plugin"
	def author = "Ken Siprell"
	def authorEmail = "ken.siprell@gmail.com"
	def developers = [
		[name: "Burt Beckwith", email: "burt@burtbeckwith.com"]
	]
	def description = "This plugin integrates the [Drools|https://www.drools.org] Business Rules Management System."
	def documentation = "https://github.com/kensiprell/grails-drools/blob/master/README.md"
	def license = "APACHE"
	def issueManagement = [system: "JIRA", url: "https://jira.grails.org/browse/GPDROOLS"]
	def scm = [url: "https://github.com/kensiprell/grails-drools"]
	def artefacts = [DroolsDomainClassArtefactHandler]

	def doWithSpring = {
		try {
			importBeans("drools-context.xml")
		} catch (e) {
			log.debug(e)
			log.error "grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
		}
	}
}
