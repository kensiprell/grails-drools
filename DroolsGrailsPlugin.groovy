import grails.plugin.drools.DroolsDomainClassArtefactHandler
import grails.spring.BeanBuilder
import org.kie.spring.KModuleBeanFactoryPostProcessor

class DroolsGrailsPlugin {

	def version = "1.1.0-SNAPSHOT"
	def grailsVersion = "2.3 > *"
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
	def pluginExcludes = [
		"grails-app/conf/drools-context.xml",
		"grails-app/conf/DroolsConfig.groovy",
		"grails-app/domain/**",
		"src/resources/rules/**"
	]
	def watchedResources = [
		"file:./grails-app/conf/DroolsConfig.groovy",
		"file:./grails-app/conf/drools-context.xml",
		"file:./src/resources/**/*.drl",
		"file:./src/resources/**/*.rule"
	]

	def doWithSpring = {
		try {
			importBeans("drools-context.xml")
		} catch (e) {
			log.debug(e)
			log.error "grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
		}

		String userDir = System.getProperty("user.dir")
		String configFilePath = "$userDir/src/resources"
		URL configFileURL = new File(configFilePath).toURI().toURL()
		kiePostProcessor(KModuleBeanFactoryPostProcessor, configFileURL, configFilePath) {}
	}

	def onChange = { event ->
		def beanBuilder = new BeanBuilder()
		String filename = event.source.filename
		File file = event.source.file

		if (filename == "drools-context.xml") {
			// TODO not needed?
		} else if (filename == "DroolsConfig.groovy") {
			// TODO call Gant script?
		} else {
			String userDir = System.getProperty("user.dir")
			String shortPath = ("${file.toString()}" - "$userDir/src/resources")
			def resourceFile = new File("$userDir/target/work/resources/$shortPath")
			resourceFile << file.text
			println "TEST: $resourceFile.text"
		}
		beanBuilder.importBeans("drools-context.xml")
	}
}
