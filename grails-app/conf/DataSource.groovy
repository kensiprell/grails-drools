dataSource {
	pooled = true
	driverClassName = 'org.h2.Driver'
	username = 'sa'
	password = ''
	dbCreate = 'update'
	url = 'jdbc:h2:mem:testDb'
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
	flush.mode = 'manual' // recommended by 2.4.3 release notes
    singleSession = true // configure OSIV singleSession mode
}
