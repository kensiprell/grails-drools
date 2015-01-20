includeTargets << grailsScript("_GrailsCreateArtifacts")
includeTargets << grailsScript('_GrailsPackage')

target(createDroolsDomain: "Creates a domain class for drools plugin rules and updates Config.groovy.") {
	depends(checkVersion, parseArguments, compile, createConfig)

	def type = "DroolsDomainClass"
	def className = ""
	promptForName(type: type)

	for (name in argsMap.params) {
		name = purgeRedundantArtifactSuffix(name, type)
		className = name
		createArtifact(name: name, suffix: "", type: type, path: "grails-app/domain")
	}

	// Update application Config.groovy if necessary
	def config = config.grails.plugin.drools.droolsRuleDomainClass
	def configGroovyFile = new File(basedir, "grails-app/conf/Config.groovy")
	if (!config) {
		configGroovyFile.withWriterAppend { BufferedWriter writer ->
			writer.newLine()
			writer.writeLine "// Added by the Drools plugin:"
			writer.writeLine "grails.plugin.drools.droolsRuleDomainClass = '$className'"
		}
		println "Please verify your grails-app/conf/Config.groovy was updated:"
		println "Added \"grails.plugin.drools.droolsRuleDomainClass = '$className'\"."
	} else if (config != className) {
		def configGroovyText = configGroovyFile.text
		def replacementText = "droolsRuleDomainClass = '${className}' // Changed by Drools plugin"
		configGroovyText = configGroovyText.replaceAll(/(?m)droolsRuleDomainClass.*$/, replacementText)
		configGroovyFile.write(configGroovyText)
		println "Please verify your grails-app/conf/Config.groovy was updated:"
		println "Changed 'grails.plugin.drools.droolsRuleDomainClass' to '$className'."
	}
}

USAGE = """
    create-drools-domain [NAME]

where
    NAME       = The name of the domain class. If not provided, this
                 command will ask you for the name.
"""

setDefaultTarget 'createDroolsDomain'
