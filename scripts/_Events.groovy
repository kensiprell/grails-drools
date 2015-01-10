import grails.util.Environment

eventCompileEnd = {
	if (!isPluginProject) {
		writeDroolsContentXml(basedir)
	}
}

// TODO if grails-app/drools is not empty

def writeDroolsContentXml(basedir) {
	def droolsContentXmlStart = """
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:kie="http://drools.org/schema/kie-spring"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
 http://drools.org/schema/kie-spring http://drools.org/schema/kie-spring.xsd">
	<kie:kmodule id="kieModule">
"""
	def droolsContentXmlEnd = """
	</kie:kmodule>
</beans>
"""
	def droolsContentXmlMiddle = getDroolsContentXmlMiddle()
	def droolsContentXml = droolsContentXmlStart + droolsContentXmlMiddle + droolsContentXmlEnd
	new File("$basedir/grails-app/conf/drools-content.xml").write droolsContentXml
}

String getDroolsContentXmlMiddle() {
	def slurper = new ConfigSlurper(Environment.current.name)
	def customConfig = null
	def vibeDefaultConfigFile = new File("${vibePluginDir}/grails-app/conf/VibeDefaultConfig.groovy").toURI().toURL()
	def vibeConfigFile = new File("${basedir}/grails-app/conf/VibeConfig.groovy").toURI().toURL()
	def defaultConfig = slurper.parse(vibeDefaultConfigFile)
	try {
		customConfig = slurper.parse(vibeConfigFile)
	}
	catch (e) {
	}
	def vibeConfig = customConfig ? defaultConfig.merge(customConfig) : defaultConfig
	def sitemeshXml = new File("$basedir/web-app/WEB-INF/sitemesh.xml")
	def rootMappings = []
	if (vibeConfig.vibe.rootMapping == "") {
		vibeConfig.values().each {
			if (it.mapping.class == String) rootMappings.add(it.mapping)
		}
	} else {
		rootMappings = ["${vibeConfig.vibe.rootMapping}/*" ?: "/vibe/*"]
	}

	// TODO Generate XML
	/*
			<kie:kbase name="kieBase" packages="grails.plugin.drools.rules">
				<kie:ksession name="kieSessionStateful"/>
				<kie:ksession name="kieSessionStateless" type="stateless"/>
			</kie:kbase>
	*/

}

/*
I'm no expert on kie-spring, but if your drl files are not on the classpath, I suggest creating a FactoryBean that generates the KieBase or the KieSession for you, and passing the list of actual paths to that factory class. Later on, you reference that bean as a KieBase or KieSession. For example, if you create a class that implements FactoryBean<KieSession>, this would be the content of the createComponent method:

KieServices ks = KieServices.Factory.get();
KieFileSystem kfs = ks.newKieFileSystem();
for (String fileName : drlFiles) {
    kfs.write("src/main/resources/org/kie/example5/"+fileName, getActualPathFromYourLegacySystem(fileName));
}
KieBuilder kb = ks.newKieBuilder(kfs);
kb.buildAll(); // kieModule is automatically deployed if successfully built.
if (kb.getResults().hasMessages(Level.ERROR)) {
    throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
}
KieContainer kContainer = ks.newKieContainer(kb.getKieModule().getReleaseId());
KieSession kSession = kContainer.newKieSession();
return ksession;

Cheers,


This example shows that it is possible to add the Kie artifacts both as plain Strings and as Resources. In the latter case the Resources can be created by the KieResources factory, also provided by the KieServices. The KieResources provides many convenient factory methods to convert an InputStream, a URL, a File, or a String representing a path of your file system to a Resource that can be managed by the KieFileSystem.

Normally the type of a Resource can be inferred from the extension of the name used to add it to the KieFileSystem. However it also possible to not follow the Kie conventions about file extensions and explicitly assign a specific ResourceType to a Resource as shown below:

Example 4.10. Creating and adding a Resource with an explicit type

KieFileSystem kfs = ...

kfs.write( "src/main/resources/myDrl.txt",

           kieServices.getResources().newInputStreamResource( drlStream )

                      .setResourceType(ResourceType.DRL) );


Example 4.62. Utilize and Run - Java

KieServices ks = KieServices.Factory.get();
KieFileSystem kfs = ks.newKieFileSystem();

Resource ex1Res = ks.getResources().newFileSystemResource(getFile("named-kiesession"));
Resource ex2Res = ks.getResources().newFileSystemResource(getFile("kiebase-inclusion"));

ReleaseId rid = ks.newReleaseId("org.drools", "kiemodulemodel-example", "6.0.0-SNAPSHOT");
kfs.generateAndWritePomXML(rid);

KieModuleModel kModuleModel = ks.newKieModuleModel();
kModuleModel.newKieBaseModel("kiemodulemodel")

            .addInclude("kiebase1")

            .addInclude("kiebase2")

            .newKieSessionModel("ksession6");


kfs.writeKModuleXML(kModuleModel.toXML());

kfs.write("src/main/resources/kiemodulemodel/HAL6.drl", getRule());


KieBuilder kb = ks.newKieBuilder(kfs);

kb.setDependencies(ex1Res, ex2Res);

kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.

if (kb.getResults().hasMessages(Level.ERROR)) {

    throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());

}


KieContainer kContainer = ks.newKieContainer(rid);


KieSession kSession = kContainer.newKieSession("ksession6");

kSession.setGlobal("out", out);


Object msg1 = createMessage(kContainer, "Dave", "Hello, HAL. Do you read me, HAL?");

kSession.insert(msg1);

kSession.fireAllRules();


Object msg2 = createMessage(kContainer, "Dave", "Open the pod bay doors, HAL.");

kSession.insert(msg2);

kSession.fireAllRules();


Object msg3 = createMessage(kContainer, "Dave", "What's the problem?");

kSession.insert(msg3);

kSession.fireAllRules();
 */