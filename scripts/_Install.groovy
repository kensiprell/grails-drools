includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
if (!droolsConfigFile.exists()) {
	copyDroolsConfig()
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
