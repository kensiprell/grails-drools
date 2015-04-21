import grails.plugin.drools.DroolsDomainClassArtefactHandler
import grails.spring.BeanBuilder
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.kie.spring.KModuleBeanFactoryPostProcessor
import org.springframework.core.io.FileSystemResource

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

		File webInfClasses = application.parentContext?.getResource('WEB-INF/classes')?.file
		if (webInfClasses?.exists()) {
			kiePostProcessor(KModuleBeanFactoryPostProcessor) {}
		} else {
			String userDir = System.getProperty("user.dir")
			String configFilePath = "$userDir/src/resources"
			URL configFileURL = new File(configFilePath).toURI().toURL()
			kiePostProcessor(KModuleBeanFactoryPostProcessor, configFileURL, configFilePath) {}
		}
	}

	def onChange = { event ->
		String appDir = System.getProperty("user.dir")
		String pluginDir = GrailsPluginUtils.getPluginDirForName("drools").file.toString()
		String filename = ""
		if (event.source instanceof FileSystemResource) {
			filename = event.source.filename
		}

		if (filename == "drools-context.xml") {
			// TODO event.source: file [/Users/Ken/Development/Plugins/grails-drools-sample/grails-app/conf/drools-context.xml]
			importBeans(true, appDir)
		}
		if (event.source instanceof Class) {
			// TODO event.source: class DroolsConfig
			def shell = new GroovyShell()
			shell.run(new File("$pluginDir/scripts/_WriteDroolsContextXml.groovy"))
			importBeans(true, appDir)
		}
		if (filename.endsWith("drl") || filename.endsWith("rule")) {
			// TODO event.source: file [/Users/Ken/Development/Plugins/grails-drools-sample/src/resources/drools-rules/ticket/ticket.drl]
			File file = event.source.file
			String shortPath = ("${file.toString()}" - "$appDir/src/resources")
			def resourceFile = new File("$appDir/target/work/resources/$shortPath")
			resourceFile.write(file.text)
			printLastModified("$appDir/target/work/resources/$shortPath")
			importBeans(false, appDir)
		}
	}

	protected static importBeans(Boolean copyDroolsContext, String appDir) {
		def beanBuilder = new BeanBuilder()
		if (copyDroolsContext) {
			def droolsContextXml = new File("$appDir/grails-app/conf/drools-context.xml")
			def droolsContextXmlTarget = new File("$appDir/target/work/resources/drools-context.xml")
			droolsContextXmlTarget.write(droolsContextXml.text)
		}
		printLastModified("$appDir/target/work/resources/drools-context.xml")
		beanBuilder.importBeans("drools-context.xml")
	}

	protected static printLastModified(String filePath) {
		def file = new File(filePath)
		println "TEST $file.name: " + new Date(file.lastModified()).format('hh:mm:ss dd MMM yyyy')
	}
}
