import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

def pluginDir = GrailsPluginUtils.getPluginDirForName("drools")

target(createDroolsConfig: "Creates a default DroolsConfig.groovy in grails-app/conf/.") {
	def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
	if (!droolsConfigFile.exists()) {
		def shell = new GroovyShell()
		shell.run(new File("$pluginDir/scripts/_CreateDroolsConfig.groovy"))
		println "Created default DroolsConfig.groovy in grails-app/conf/."
	} else {
		println "Error: grails-app/conf/DroolsConfig.groovy exists and was not overwritten."
	}
}

USAGE = """
    create-drools-config
"""

setDefaultTarget 'createDroolsConfig'
