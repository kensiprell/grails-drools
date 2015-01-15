kieBases = [
	[
		includeInConfig: true,
		attributes     : [
			name    : "applicationKieBase",
			default : false,
			packages: "rules.application"
		],
		kieSessions    : [
			[
				includeInConfig: true,
				attributes     : [
					name   : "applicationStatefulSession",
					default: false,
					type   : "stateful"
				]
			],
			[
				includeInConfig: true,
				attributes     : [
					name   : "applicationStatelessSession",
					default: false,
					type   : "stateless"
				]
			]
		]
	],
	[
		includeInConfig: true,
		attributes     : [
			name    : "ticketKieBase",
			default : false,
			packages: "rules.ticket"
		],
		kieSessions    : [
			[
				includeInConfig: true,
				attributes     : [
					name   : "ticketStatefulSession",
					default: false,
					type   : "stateful"
				]
			],
			[
				includeInConfig: true,
				attributes     : [
					name   : "ticketStatelessSession",
					default: false,
					type   : "stateless"
				]
			]
		]
	]
]



