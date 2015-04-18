import grails.plugin.drools.DroolsDomainClassArtefactHandler
import org.kie.spring.KModuleBeanFactoryPostProcessor

class DroolsGrailsPlugin {

	def version = "1.0.1-SNAPSHOT"
	def grailsVersion = "2.3 > *"
	def pluginExcludes = [
		"grails-app/conf/drools-context.xml",
		"grails-app/conf/DroolsTestConfig.groovy",
		"grails-app/domain/**",
		"src/resources/rules/**"
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
	def issueManagement = [system: "github", url: "https://github.com/kensiprell/grails-drools/issues"]
	def scm = [url: "https://github.com/kensiprell/grails-drools"]
	def artefacts = [DroolsDomainClassArtefactHandler]

	def doWithSpring = {
		try {
			importBeans("drools-context.xml")
		} catch (e) {
			log.debug(e)
			log.error "grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
		}

		File webInfClasses = application.parentContext?.getResource('WEB-INF/classes')?.file
		if (webInfClasses.exists()) {
			kiePostProcessor(KModuleBeanFactoryPostProcessor) {}
		} else {
			String userDir = System.getProperty("user.dir")
			String configFilePath = "$userDir/src/resources"
			URL configFileURL = new File(configFilePath).toURI().toURL()
			kiePostProcessor(KModuleBeanFactoryPostProcessor, configFileURL, configFilePath) {}
		}
	}
}
