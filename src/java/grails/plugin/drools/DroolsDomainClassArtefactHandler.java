package grails.plugin.drools;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;

public class DroolsDomainClassArtefactHandler extends ArtefactHandlerAdapter {

	public static final String TYPE = "DroolsDomainClass";

	public static final String SUFFIX = "DroolsDomainClass";

	public static interface DroolsDomainClass extends GrailsClass {}

	public static class DefaultDroolsDomainClass extends AbstractGrailsClass implements DroolsDomainClass {

		public DefaultDroolsDomainClass(Class<?> clazz) {
			super(clazz, DroolsDomainClassArtefactHandler.SUFFIX);
		}

		public String getKey() {
			Object key = GrailsClassUtils.getStaticPropertyValue(getClazz(), "key");
			return (key == null) ? null : key.toString();
		}
	}

	public DroolsDomainClassArtefactHandler() {
		super(TYPE, DroolsDomainClass.class, DefaultDroolsDomainClass.class, SUFFIX);
	}
}
