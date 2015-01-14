// TODO Comment
kieBases = [
	[
		// TODO Comment
		includeInConfig: true,
		// TODO Comment
		attributes     : [
			name    : "defaultKieBase", // Required
			default : true, // TODO Comment
			packages: "test.with.defaultKieBase", // Comma separated list of resource packages to be included in this kbase.
			//includes           : "", // All resources from the corresponding kbases are included in this kbase.
			//scope              : "", // TODO Comment
			//eventProcessingMode: "", // Valid options are STREAM, CLOUD.
			//equalsBehavior     : "", // Valid options are IDENTITY, EQUALITY.
			//declarativeAgenda  : "", // Valid options are enabled, disabled, true, false.
		],
		// TODO Comment
		kieSessions    : [
			[
				// TODO Comment
				includeInConfig: true,
				// TODO Comment
				attributes     : [
					name   : "defaultKieStatefulSession", // Required
					default: true, // TODO Comment
					type   : "stateful", // Valid options are stateful, stateless.
					//scope          : "", // TODO Comment
					//clockType      : "", // Valid options are REALTIME, PSEUDO.
					//"listeners-ref": ""  // Specifies a reference to an event listener group.
				],
				// TODO Comment
				// All map keys are required values if includeInConfig is set to true.
				// The referenced bean must be...
				kieListeners   : [
					[
						includeInConfig: false, // Required
						type           : "", // Required: valid options are agendaEventListener, processEventListener, ruleRuntimeEventListener.
						// TODO Below can be used separately or together; comment out if not needed
						ref            : "", // TODO valid options ...
						nestedBeanClass: "",  // TODO nested bean
						debug          : false, // TODO
					]
				]
			],
			[
				// TODO Comment
				includeInConfig: true,
				// TODO Comment
				attributes     : [
					name   : "defaultKieStatelessSession", // Required
					default: true, // TODO Comment
					type   : "stateless", // Valid options are stateful, stateless.
					//scope          : "", // TODO Comment
					//clockType      : "", // Valid options are REALTIME, PSEUDO.
					//"listeners-ref": ""  // Specifies a reference to an event listener group.
				]
			]
		]
	]
]

// TODO doWithSpring iterates over these
// TODO Comment The event listeners will be created as beans at runtime
kieEventListeners = [
	[
		// TODO Comment
		includeInConfig         : false,
		name                    : "",
		agendaEventListener     : [includeInConfig: false, id: "mock-agenda-listener", class: "package.name.class"],
		processEventListener    : [includeInConfig: false, id: "mock-process-listener", class: "package.name.class"],
		ruleRuntimeEventListener: [includeInConfig: false, id: "mock-rr-listener", class: "package.name.class"]
	]
]



