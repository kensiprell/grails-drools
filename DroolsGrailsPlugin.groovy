import grails.plugin.drools.DroolsDomainClassArtefactHandler
import grails.util.Environment
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.kie.api.builder.KieFileSystem
import org.kie.api.io.KieResources
import org.kie.api.io.Resource
import org.kie.api.KieServices
import org.kie.spring.KModuleBeanFactoryPostProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
	private Logger logger = LoggerFactory.getLogger("org.grails.plugins.drools.DroolsGrailsPlugin")
	private grailsApplication
	private beanBuilder

	def doWithSpring = {
		this.grailsApplication = application
		this.beanBuilder = delegate
		buildBeans()
	}

	def onChange = { event ->
		// TODO test thoroughly in Windows
		if (Environment.current != Environment.DEVELOPMENT) return
		String appDir = System.getProperty("user.dir")
		String pluginDir = GrailsPluginUtils.getPluginDirForName("drools").file.canonicalPath

		if (event.source instanceof Class) {
			if (event.source.name == "DroolsConfig") {
				def shell = new GroovyShell()
				shell.run(new File("$pluginDir/scripts/_WriteDroolsContextXml.groovy"))
				copyDroolsContext(appDir)
			}
		}
		if (event.source instanceof FileSystemResource) {
			String filename = event.source.filename
			if (filename == "drools-context.xml") {
				removeBeans()
				copyDroolsContext(appDir)
				buildBeans()
			}
			if (filename.endsWith("drl") || filename.endsWith("rule")) {
				File sourceFile = event.source.file
				String shortCanonicalPath = (sourceFile.canonicalPath - (new File("$appDir/src/resources").canonicalPath))
				String shortPath = shortCanonicalPath.replaceAll("\\\\", "/").replace("/", "")
				File targetFile = new File("$appDir/target/work/resources/$shortPath")
				targetFile.write(sourceFile.text)
				KieServices kieServices = KieServices.Factory.get()
				KieResources kieResources = kieServices.getResources()
				KieFileSystem kieFileSystem = kieServices.newKieFileSystem()
				Resource resource = kieResources.newClassPathResource(shortPath)
				String resourcePath = "src/main/resources/$shortPath"
				kieFileSystem.write(resourcePath, resource)
				// TODO Necessary? importBeans(false, appDir)
				logger.info "$shortCanonicalPath copied to classpath."
			}
		}
	}

	protected copyDroolsContext(String appDir) {
		def droolsContextXml = new File("$appDir/grails-app/conf/drools-context.xml")
		def droolsContextXmlTarget = new File("$appDir/target/work/resources/drools-context.xml")
		droolsContextXmlTarget.write(droolsContextXml.text)
		logger.info "drools-context.xml copied to classpath."
	}

/*
2015-Mai-02 18:47:49,905 INFO [FileSystemWatcher: files=#201 cl=java.net.URLClassLoader@5ce1b5b2] org.grails.plugins.drools.DroolsGrailsPlugin  - ? ? - drools-context.xml copied to classpath.
2015-Mai-02 18:47:59,432 INFO [Thread-16] org.grails.plugins.drools.DroolsGrailsPlugin  - ? ? - drools-context.xml copied to classpath.
2015-Mai-02 18:47:59,459 INFO [Thread-16] org.grails.plugins.drools.DroolsGrailsPlugin  - ? ? - drools-context.xml beans re-imported.
2015-Mai-02 18:47:59,460 INFO [Thread-16] org.grails.plugins.drools.DroolsGrailsPlugin  - NativeMethodAccessorImpl.java ? - Configured kiePostProcessor bean with path /Users/Ken/Development/Plugins/grails-drools-sample/src/resources
2015-Mai-02 18:48:08,908 ERROR [http-nio-8080-exec-10] org.codehaus.groovy.grails.commons.spring.OptimizedAutowireCapableBeanFactory  - SLF4JLog.java 200 - Bean couldn't be autowired using grails optimization: Error creating bean with name 'ticketStatefulSession': Cannot resolve reference to bean 'ticketKieBase' while setting bean property 'kBase'; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'ticketKieBase' is defined
2015-Mai-02 18:48:08,908 ERROR [http-nio-8080-exec-10] org.codehaus.groovy.grails.commons.spring.OptimizedAutowireCapableBeanFactory  - SLF4JLog.java 200 - Retrying using spring autowire
2015-Mai-02 18:48:08,909 ERROR [http-nio-8080-exec-10] org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/grails-drools-sample].[grails]  - ApplicationDispatcher.java 748 - Servlet.service()
 */

	protected buildBeans() {
		try {
			beanBuilder.importBeans("drools-context.xml")
			logger.info "drools-context.xml beans re-imported."
		} catch (e) {
			logger.debug e.toString()
			println "DroolsGrailsPlugin: grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
		}
		File webInfClasses = grailsApplication.parentContext?.getResource('WEB-INF/classes')?.file
		String configFilePath
		if (webInfClasses?.exists()) {
			configFilePath = new File(webInfClasses.path).canonicalPath
		} else {
			String userDir = System.getProperty("user.dir")
			configFilePath = new File("$userDir/src/resources").canonicalPath
		}
		URL configFileURL = new File(configFilePath).toURI().toURL()
		beanBuilder.kiePostProcessor(KModuleBeanFactoryPostProcessor, configFileURL, configFilePath) {}
		logger.info "Configured kiePostProcessor bean with path $configFilePath"
	}

	protected removeBeans() {
		def context =  grailsApplication.mainContext
		def factory = context.getAutowireCapableBeanFactory()
		def beanClasses = [
			"org.drools.core.impl.StatelessKnowledgeSessionImpl",
			"org.drools.core.impl.KnowledgeBaseImpl",
			"org.drools.core.impl.StatefulKnowledgeSessionImpl"
		]
		try {
			factory.removeBeanDefinition("kiePostProcessor")
			logger.info "Removed kiePostProcessor bean."
		} catch(e) {
			logger.debug e.toString()
		}

		context.getBeanDefinitionNames().each {
			if (it instanceof String) {
				try {
					def bean = context.getBean(it)
					if (beanClasses.contains(bean.class.name)) {
						factory.removeBeanDefinition(it)
						logger.info "Removed $it bean."
					}
				} catch (e) {
					// Avoid "Error creating bean with name 'abstractViewResolver': Bean definition is abstract"
					logger.debug e.toString()
				}
			}
		}
	}
}
