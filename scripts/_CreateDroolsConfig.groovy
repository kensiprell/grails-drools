import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

// Copies default DroolsConfig.groovy to grails-app/conf"
def appDir = System.getProperty("user.dir")
def pluginDir = GrailsPluginUtils.getPluginDirForName("drools")
def source = new File("${pluginDir}/src/templates/conf/DroolsConfig.groovy")
def target = new File("${appDir}/grails-app/conf/DroolsConfig.groovy")
target << source.text

