includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

target(createDroolsConfig: "Creates a default DroolsConfig.groovy in grails-app/conf/.") {
	def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
	if (!droolsConfigFile.exists()) {
		copyDroolsConfig()
		println "Created default DroolsConfig.groovy in grails-app/conf/."
	} else {
		println "Error: grails-app/conf/DroolsConfig.groovy exists and was not overwritten."
	}
}

USAGE = """
    create-drools-config
"""

setDefaultTarget 'createDroolsConfig'
