includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
if (!droolsConfigFile.exists()) {
	copyDroolsConfig()
}

println """
*******************************************************
* You have installed the drools plugin.               *
*                                                     *
* Documentation:                                      *
* https://github.com/kensiprell/grails-drools         *
*                                                     *
* Next step:                                          *
* grails create-drools-domain com.example.DroolsRule  *
*                                                     *
*******************************************************
"""
