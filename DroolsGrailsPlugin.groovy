import grails.plugin.drools.DroolsDomainClassArtefactHandler
import org.kie.api.builder.KieFileSystem
import org.kie.api.io.KieResources
import org.kie.api.io.Resource
import org.kie.api.KieServices
import org.kie.spring.KModuleBeanFactoryPostProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource

class DroolsGrailsPlugin {

	def version = "1.1.0"
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
	private Logger logger = LoggerFactory.getLogger("org.grails.plugins.drools.DroolsGrailsPlugin")

	def doWithSpring = {
		try {
			importBeans("drools-context.xml")
			logger.info "drools-context.xml beans imported."
		} catch (e) {
			logger.debug e.toString()
			println "DroolsGrailsPlugin: grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
		}

		File webInfClasses = application.parentContext?.getResource('WEB-INF/classes')?.file
		String configFilePath
		if (webInfClasses?.exists()) {
			configFilePath = new File(webInfClasses.path).canonicalPath
		} else {
			String userDir = System.getProperty("user.dir")
			configFilePath = new File("$userDir/src/resources").canonicalPath
		}
		URL configFileURL = new File(configFilePath).toURI().toURL()
		kiePostProcessor(KModuleBeanFactoryPostProcessor, configFileURL, configFilePath) {}
		logger.info "Configured kiePostProcessor bean with path $configFilePath"
	}

	def onChange = { event ->
		String appDir = System.getProperty("user.dir")

		if (event.source instanceof FileSystemResource) {
			String filename = event.source.filename
			if (filename == "drools-context.xml") {
				println "DroolsGrailsPlugin: the application must be restarted for changes to drools-context.xml to take effect."
			}
			if (filename.endsWith("drl") || filename.endsWith("rule")) {
				File sourceFile = event.source.file
				String shortCanonicalPath = (sourceFile.canonicalPath - (new File("$appDir/src/resources").canonicalPath))
				String shortPath = shortCanonicalPath.replaceAll("\\\\", "/")
				if (shortPath.startsWith("/")) {
					shortPath = shortPath.substring(1)
				}
				File targetFile = new File("$appDir/target/work/resources/$shortPath")
				targetFile.write(sourceFile.text)
				KieServices kieServices = KieServices.Factory.get()
				KieResources kieResources = kieServices.getResources()
				KieFileSystem kieFileSystem = kieServices.newKieFileSystem()
				Resource resource = kieResources.newClassPathResource(shortPath)
				String resourcePath = "src/main/resources/$shortPath"
				kieFileSystem.write(resourcePath, resource)
				logger.info "$shortCanonicalPath copied to classpath."
			}
		}
	}
}
