target(createDroolsContext: "Creates a default drools-context.xml in grails-app/conf/.") {
	def droolsContextFile = new File(basedir, "grails-app/conf/drools-context.xml")
	if (!droolsContextFile.exists()) {
		ant.copy(file: "$droolsPluginDir/src/templates/conf/drools-context.xml", todir: "$basedir/grails-app/conf")
		println "Created default drools-context.xml in grails-app/conf/."
	} else {
		println "Error: grails-app/conf/drools-context.xml exists and was not overwritten."
	}
}

USAGE = """
    create-drools-context
"""

setDefaultTarget 'createDroolsContext'
