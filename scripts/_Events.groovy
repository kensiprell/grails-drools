import static groovy.io.FileType.FILES

includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

configurationType = grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"
drlFileLocation = grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "rules"

eventCompileEnd = {
	if (configurationType == "droolsConfigGroovy") {
		writeDroolsContentXml()
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

