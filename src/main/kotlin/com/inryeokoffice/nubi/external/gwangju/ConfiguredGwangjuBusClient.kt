package com.inryeokoffice.nubi.external.gwangju

import com.inryeokoffice.nubi.config.GwangjuBusProperties
import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.RouteStop
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class ConfiguredGwangjuBusClient(
    private val properties: GwangjuBusProperties,
    private val observedClient: ObservedWebGwangjuBusClient,
    private val officialClient: OfficialOpenApiGwangjuBusClient,
) : GwangjuBusClient {
    private val delegate: GwangjuBusClient
        get() =
            if (properties.dataSource == DataSource.OFFICIAL_OPEN_API.value) {
                officialClient
            } else {
                observedClient
            }

    override val dataSource: DataSource
        get() = delegate.dataSource

    override fun fetchRoutes(): List<BusRoute> = delegate.fetchRoutes()

    override fun fetchStops(): List<BusStop> = delegate.fetchStops()

    override fun fetchRouteStops(routeId: Long): List<Pair<RouteStop, BusStop>> = delegate.fetchRouteStops(routeId)

    override fun fetchArrivals(stopId: Long): List<BusArrival> = delegate.fetchArrivals(stopId)

    override fun fetchPositions(routeId: Long): List<BusPosition> = delegate.fetchPositions(routeId)
}
