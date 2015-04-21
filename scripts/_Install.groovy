import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
def pluginDir = GrailsPluginUtils.getPluginDirForName("drools")

if (!droolsConfigFile.exists()) {
	def shell = new GroovyShell()
	shell.run(new File("$pluginDir/scripts/_CreateDroolsConfig.groovy"))
}

println """
****************************************************************
*                           WARNING!                           *
*                                                              *
*                Drools Plugin Breaking Changes                *
*                                                              *
* If you are upgrading from 0.9.x, please visit the url below: *
* https://github.com/kensiprell/grails-drools#breaking-changes *
*                                                              *
*                           WARNING!                           *
****************************************************************
"""
