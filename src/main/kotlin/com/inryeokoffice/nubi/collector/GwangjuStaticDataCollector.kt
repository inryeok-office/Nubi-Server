package com.inryeokoffice.nubi.collector

import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.external.gwangju.GwangjuBusClient
import com.inryeokoffice.nubi.repository.BusRouteRepository
import com.inryeokoffice.nubi.repository.BusStopRepository
import com.inryeokoffice.nubi.repository.RouteStopRepository
import org.springframework.stereotype.Service

data class CollectionSummary(
    val routeCount: Int,
    val stopCount: Int,
    val routeStopCount: Int,
)

@Service
class GwangjuStaticDataCollector(
    private val client: GwangjuBusClient,
    private val routeRepository: BusRouteRepository,
    private val stopRepository: BusStopRepository,
    private val routeStopRepository: RouteStopRepository,
) {
    fun collect(): CollectionSummary {
        val routes = client.fetchRoutes()
        routes.forEach(routeRepository::upsert)

        val stops = client.fetchStops()
        stops.forEach(stopRepository::upsert)

        var routeStopCount = 0
        routes.forEach { route: BusRoute ->
            client.fetchRouteStops(route.routeId).forEach { (routeStop, stop) ->
                stopRepository.upsert(stop)
                routeStopRepository.upsert(routeStop)
                routeStopCount++
            }
        }
        return CollectionSummary(routes.size, stops.size, routeStopCount)
    }
}
