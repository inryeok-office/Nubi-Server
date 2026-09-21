package com.inryeokoffice.nubi.external.gwangju

import com.inryeokoffice.nubi.config.GwangjuBusProperties
import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.RouteStop
import org.springframework.stereotype.Component

/**
 * Adapter boundary for the data.go.kr contract.
 * The research report does not contain the authenticated endpoint contract, so no endpoint is guessed here.
 */
@Component
class OfficialOpenApiGwangjuBusClient(
    private val properties: GwangjuBusProperties,
) : GwangjuBusClient {
    override val dataSource: DataSource = DataSource.OFFICIAL_OPEN_API

    override fun fetchRoutes(): List<BusRoute> = unavailable()

    override fun fetchStops(): List<BusStop> = unavailable()

    override fun fetchRouteStops(routeId: Long): List<Pair<RouteStop, BusStop>> = unavailable()

    override fun fetchArrivals(stopId: Long): List<BusArrival> = unavailable()

    override fun fetchPositions(routeId: Long): List<BusPosition> = unavailable()

    private fun unavailable(): Nothing {
        val detail =
            if (properties.apiKey.isBlank()) {
                "GWANGJU_BUS_API_KEY is not configured"
            } else {
                "the authenticated data.go.kr endpoint contract is not configured"
            }
        throw GwangjuExternalApiException("official-open-api is unavailable: $detail")
    }
}
