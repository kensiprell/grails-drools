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
* See the link below for details:                              *
* https://github.com/kensiprell/grails-drools#breaking-changes *
*                                                              *
*                           WARNING!                           *
****************************************************************
"""
