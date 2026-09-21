package com.inryeokoffice.nubi.api

import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.NearbyBusStop
import com.inryeokoffice.nubi.external.gwangju.GwangjuBusClient
import com.inryeokoffice.nubi.repository.BusRouteRepository
import com.inryeokoffice.nubi.repository.BusStopRepository
import com.inryeokoffice.nubi.repository.RouteStopRepository
import org.springframework.stereotype.Service
import java.time.Instant

data class ArrivalsSnapshot(
    val stopId: Long,
    val dataSource: String,
    val fetchedAt: Instant,
    val arrivals: List<BusArrival>,
)

@Service
class NubiQueryService(
    private val routeRepository: BusRouteRepository,
    private val stopRepository: BusStopRepository,
    private val routeStopRepository: RouteStopRepository,
    private val busClient: GwangjuBusClient,
) {
    fun findNearbyStops(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        limit: Int,
    ): List<NearbyBusStop> = stopRepository.findNearby(latitude, longitude, radiusMeters, limit)

    fun findStop(stopId: Long): BusStop = stopRepository.findById(stopId) ?: throw ResourceNotFoundException("Bus stop not found: $stopId")

    fun findArrivals(stopId: Long): ArrivalsSnapshot {
        findStop(stopId)
        val fetchedAt = Instant.now()
        return ArrivalsSnapshot(stopId, busClient.dataSource.value, fetchedAt, busClient.fetchArrivals(stopId))
    }

    fun findRoute(routeId: Long): BusRoute =
        routeRepository.findById(routeId) ?: throw ResourceNotFoundException("Bus route not found: $routeId")

    fun findRouteStops(routeId: Long) =
        routeStopRepository.findByRouteId(routeId).also {
            if (routeRepository.findById(routeId) == null) {
                throw ResourceNotFoundException("Bus route not found: $routeId")
            }
        }
}
