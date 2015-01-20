/**
 * This file is parsed at compile time and is used to create grails-app/conf/drools-context.xml,
 * which is used at runtime to import the Kie beans.
 *
 * The kiePostProcessor bean, which is required for kie-spring to function properly, is included automatically:
 * <bean id='kiePostProcessor' class='org.kie.spring.KModuleBeanFactoryPostProcessor'/>
 *
 * http://docs.jboss.org/drools/release/6.1.0.Final/drools-docs/html/ch.kie.spring.html
 */

/**
 * kie:kbase elements to include in drools-context.xml.
 */
kieBases = [
	[
		/**
		 * If includeInConfig is set to false, this kbase will not be included in drools-context.xml.
		 * You can use this option to disable a kbase without having to comment out or delete its entire section.
		 */
		includeInConfig: true,
		/**
		 * The attributes below are included exactly as shown.
		 * Comment out a line to prevent its inclusion; do not simply insert a null value.
		 * name is the only required attribute; the rest are optional.
		 */
		attributes     : [
			name               : "defaultKieBase", // Required
			default            : false,             // Determines if this is the default kbase.
			//packages           : "defaultKieBase", // Comma separated list of resource packages to be included in this kbase.
			//includes           : "",               // All resources from the corresponding kbases are included in this kbase.
			//eventProcessingMode: "",               // Valid options are STREAM, CLOUD.
			//equalsBehavior     : "",               // Valid options are IDENTITY, EQUALITY.
			//declarativeAgenda  : "",               // Valid options are enabled, disabled, true, false.
		],
		/**
		 * kie:ksession elements to include in this kbase.
		 */
		kieSessions    : [
			[
				/**
				 * If includeInConfig is set to false, this ksession will not be included in this kbase.
				 * You can use this option to disable a ksession without having to comment out or delete its entire section.
				 */
				includeInConfig: true,
				/**
				 * The attribute values below are included exactly as shown.
				 * Comment out a line to prevent its inclusion; do not simply insert a null value.
				 * name is the only required attribute; the rest are optional.
				 */
				attributes     : [
					name           : "defaultKieStatefulSession", // Required
					default        : true,                        // Determines if this is the default ksession for this kbase.
					type           : "stateful",                  // Valid options are stateful, stateless.
					//clockType      : "",                          // Valid options are REALTIME, PSEUDO.
					//"listeners-ref": ""                           // Specifies a reference to an event listener group.
				],
				/**
				 * Event listener elements to include in this ksession.
				 */
				kieListeners   : [
					[
						/**
						 * If includeInConfig is set to false, this listener will not be included in this ksession.
						 * You can use this option to disable a listener without having to comment out or delete its entire section.
						 */
						includeInConfig: false,
						/**
						 * This option is required if includeInConfig is set to true.
						 * Valid options are agendaEventListener, processEventListener, ruleRuntimeEventListener.
						 */
						type           : "agendaEventListener",
						/**
						 * Setting this plugin option to true will use the debug version provided by the Knowledge-API.
						 * If set to true, the ref and nestedBeanClass options below will be ignored.
						 */
						debug          : false,
						/**
						 * The ref option must refer to a valid event listener bean or event listener group.
						 * Ensure debug is set to false and the nestedBeanClass line below is commented out.
						 * Event listener beans are configured in the kieEventListeners List.
						 * Event listener groups are configured in the kieEventListenerGroups List.
						 */
						//ref            : "beanNameFromKieEventListenersList",
						/**
						 * This plugin option provides a ksession event listener bean without having to define it in kieEventListeners.
						 * Ensure debug is set to false and the ref line above is commented out.
						 * It will insert the following element:
						 * <kie:agendaEventListener>
						 *     <bean class="com.example.MyAgendaEventListener"/>
						 * </kie:agendaEventListener>
						 */
						nestedBeanClass: "com.example.MyAgendaEventListener"
					]
				]
			],
			/**
			 * kie:ksession configuration without comments.
			 */
			[
				includeInConfig: true,
				attributes     : [
					name   : "defaultKieStatelessSession",
					default: true,
					type   : "stateless",
				]
			]
		]
	]
]

/**
 * Event listener elements to include in drools-context.xml.
 * The configuration below would result in the following root elements
 * if the includeInConfig values were set to true:
 *
 * <bean id='myAgendaListener'  class='com.example.AgendaEventListener'/>
 * <bean id='myProcessListener' class='com.example.ProcessEventListener'/>
 * <bean id='myRuntimeListener' class='com.example.RuleRuntimeEventListener'/>
 */
kieEventListeners = [
	[
		/**
		 * If includeInConfig is set to true, the attributes below are included exactly as shown.
		 * If includeInConfig is set to false, this listener will not be included.
		 * id is the bean name.
		 * class must reference a valid class on the classpath.
		 */
		includeInConfig: false,
		attributes      : [
			id   : "myAgendaListener",
			class: "com.example.AgendaEventListener"
		]
	],
	[
		includeInConfig: false,
		attributes      : [
			id   : "myProcessListener",
			class: "com.example.ProcessEventListener"
		]
	],
	[
		includeInConfig: false,
		attributes      : [
			id   : "myRuntimeListener",
			class: "com.example.RuleRuntimeEventListener"
		]
	]
]

/**
 * Event listener group elements to include in drools-context.xml.
 * The configuration below would result in the following root element
 * if the includeInConfig values were set to true:
 *
 * <kie:eventListeners id='myListenerGroup'>
 *     <kie:agendaEventListener ref='myAgendaListener'/>
 *     <kie:processEventListener ref='myProcessListener'/>
 *     <kie:ruleRuntimeEventListener ref='myRuntimeListener'/>
 * </kie:eventListeners>
 */
kieEventListenerGroups = [
	[
		/**
		 * If includeInConfig is set to false, this listener group will not be included
		 * regardless of the includeInConfig settings for the individual listeners.
		 * id is the bean name.
		 */
		includeInConfig: false,
		id             : "myListenerGroup",
		listeners      : [
			[
				/**
				 * If includeInConfig is set to false, this listener will not be included.
				 * If includeInConfig is set to true, type and ref are required.
				 * type options are agendaEventListener, processEventListener, ruleRuntimeEventListener.
				 * ref must refer to a valid bean defined in kieEventListeners.
				 */
				includeInConfig: true,
				type           : "agendaEventListener",
				ref            : "myAgendaListener"
			],
			[
				includeInConfig: true,
				type           : "processEventListener",
				ref            : "myProcessListener"
			],
			[
				includeInConfig: true,
				type           : "ruleRuntimeEventListener",
				ref            : "myRuntimeListener"
			]
		]
	]
]
