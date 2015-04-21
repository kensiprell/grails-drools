import grails.util.Environment
import static groovy.io.FileType.FILES

configurationType = grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"
drlFileLocation = grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "rules"

eventCompileEnd = {
	if (configurationType == "droolsConfigGroovy") {
		def shell
		if (Environment.current == Environment.TEST) {
			shell = new GroovyShell(new GroovyClassLoader())
		} else {
			shell = new GroovyShell()
		}
		shell.run(new File("$droolsPluginDir/scripts/_WriteDroolsContextXml.groovy"))
	}
	copyFiles(buildSettings.resourcesDir)
}

eventCreateWarStart = { warName, stagingDir ->
	copyFiles("$stagingDir/WEB-INF/classes")
}

// Copy *.drl and *.rule files
private void copyFiles(destination) {
	def sourceDir = new File(basedir, "src/resources/$drlFileLocation")
	String drlFileLocationPath = new File("$basedir/src/resources/$drlFileLocation").canonicalPath
	def nameFilterRules = ~/.*\.(drl|rule)$/
	sourceDir.traverse(type: FILES, nameFilter: nameFilterRules) {
		String filePath = new File(it.path).canonicalPath
		String newName = ("$drlFileLocation$filePath" - drlFileLocationPath)
		def newFile = new File(destination, newName)
		newFile.parentFile.mkdirs()
		newFile.write(it.text)
	}
}
