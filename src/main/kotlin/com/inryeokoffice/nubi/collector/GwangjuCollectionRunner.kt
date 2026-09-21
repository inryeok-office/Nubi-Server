package com.inryeokoffice.nubi.collector

import com.inryeokoffice.nubi.config.GwangjuBusProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class GwangjuCollectionRunner(
    private val properties: GwangjuBusProperties,
    private val collector: GwangjuStaticDataCollector,
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        if (!properties.collectionEnabled) return
        val summary = collector.collect()
        logger.info(
            "Gwangju static data collection completed: routes={}, stops={}, routeStops={}, source={}",
            summary.routeCount,
            summary.stopCount,
            summary.routeStopCount,
            properties.dataSource,
        )
    }
}
