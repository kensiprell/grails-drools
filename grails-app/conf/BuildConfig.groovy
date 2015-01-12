grails.project.work.dir = "target"
grails.project.docs.output.dir = "docs/manual" // for backwards-compatibility, the docs are checked into gh-pages branch
grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
	inherits "global"
	log "warn"

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		String droolsVersion = "6.1.0.Final"
		String comSunXmlBindVersion = "2.2.11"

		compile "org.drools:drools-compiler:$droolsVersion", {
			excludes "activation", "antlr-runtime", "cdi-api", "drools-core", "ecj", "glazedlists_java15",
				"gunit", "janino", "junit", "logback-classic", "mockito-all", "mvel2",
				"org.osgi.compendium", "org.osgi.core", "quartz", "slf4j-api",
				"stax-api", "weld-se-core", "xstream"
		}
		compile "org.drools:drools-core:$droolsVersion", {
			excludes "activation", "antlr", "antlr-runtime", "cdi-api", "junit", "kie-api", "kie-internal",
				"logback-classic", "mockito-all", "mvel2", "org.osgi.compendium", "org.osgi.core",
				"protobuf-java", "slf4j-api", "stax-api", "xstream"    //
		}
		compile "org.drools:drools-decisiontables:$droolsVersion", {
			excludes "commons-io", "drools-compiler", "drools-core", "drools-templates", "junit", "logback-classic",
				"mockito-all", "org.osgi.compendium", "org.osgi.core", "poi-ooxml", "slf4j-api"
		}
		compile "org.drools:drools-jsr94:$droolsVersion", {
			excludes "drools-compiler", "drools-core", "drools-decisiontables", "jsr94", "jsr94-sigtest",
				"jsr94-tck", "junit", "mockito-all"
		}
		compile "org.drools:drools-verifier:$droolsVersion", {
			excludes "drools-compiler", "guava", "itext", "junit", "kie-api", "mockito-all", "xstream"
		}
		compile "org.kie:kie-api:$droolsVersion", {
			excludes "activation", "cdi-api", "jms", "junit", "mockito-all", "org.osgi.compendium",
				"org.osgi.core", "quartz", "slf4j-api", "stax-api", "xstream"
		}
		compile "org.kie:kie-internal:$droolsVersion", {
			excludes "cdi-api", "junit", "kie-api", "mockito-all", "slf4j-api", "xstream"
		}

		compile "org.kie:kie-spring:$droolsVersion"

		runtime "com.sun.xml.bind:jaxb-xjc:$comSunXmlBindVersion", {
			excludes "jaxb-core"
		}
		runtime "com.sun.xml.bind:jaxb-impl:$comSunXmlBindVersion", {
			excludes "FastInfoset", "jaxb-core"
		}

		// TODO find more recent version
		// java.lang.NoClassDefFoundError: org/codehaus/janino/Scanner$ScanException in 2.7.6 and above
		runtime "org.codehaus.janino:janino:2.5.16", {
			excludes "ant-nodeps", "junit"
		}

		runtime "com.thoughtworks.xstream:xstream:1.4.7", {
			excludes "cglib-nodep", "commons-lang", "dom4j", "jdom", "jettison", "jmock", "joda-time",
				"json", "junit", "kxml2", "kxml2-min", "oro", "stax", "stax-api",
				"wstx-asl", "xml-writer", "xmlpull", "xom", "xpp3_min"
		}

		runtime "org.eclipse.jdt.core.compiler:ecj:4.4"

		runtime "org.mvel:mvel2:2.2.2.Final", {
			excludes "junit", "xstream"
		}

		runtime "org.antlr:antlr-runtime:3.5.2", {
			excludes "junit", "stringtemplate"
		}

		//		drools-persistence-jpa-6.0.0.CR5
		//		drools-templates-6.0.0.CR5
		//		bcmail-jdk14-138
		//		bcprov-jdk14-138
		//		dom4j-1.6.1
		//		guava-13.0.1
		//		hibernate-jpa-2.0-api-1.0.1.Final
		//		itext-2.1.2
		//		javassist-3.15.0-GA
		//		jsr94-1.1
		//		poi-3.9
		//		poi-ooxml-3.9
		//		poi-ooxml-schemas-3.9
		//		stax-api-1.0.1
		//		xml-apis-1.3.04
		//		xmlbeans-2.3.0
		//		xmlpull-1.1.3.1
	}

	plugins {
		build ":release:3.0.1", ":rest-client-builder:2.0.3", {
			export = false
		}

		runtime ":hibernate4:4.3.6.1", {
			export = false
		}
	}
}
