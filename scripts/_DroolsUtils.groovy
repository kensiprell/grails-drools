target(copyDroolsConfig: "Copies default DroolsConfig.groovy to grails-app/conf") {
	ant.copy(file: "${droolsPluginDir}/src/templates/conf/DroolsConfig.groovy", todir: "${basedir}/grails-app/conf")
}
