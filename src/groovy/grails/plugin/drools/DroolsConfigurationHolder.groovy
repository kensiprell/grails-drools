package grails.plugin.drools

import grails.util.Environment
import grails.util.Holders
import groovy.util.logging.Log4j

@Log4j
@Singleton
class DroolsConfigurationHolder {

	static ConfigObject getPluginConfig() {
		GroovyClassLoader classLoader = new GroovyClassLoader()
		def slurper = new ConfigSlurper(Environment.current.name)
		def defaultConfigClass = classLoader.loadClass("DroolsDefaultConfig")
		def defaultConfig = slurper.parse(defaultConfigClass).grails.plugin.drools
		def grailsApplication = Holders.grailsApplication
		def customConfig = grailsApplication.config.grails.plugin?.drools
		def config = customConfig ? defaultConfig.merge(customConfig) : defaultConfig
		return config
	}
}
