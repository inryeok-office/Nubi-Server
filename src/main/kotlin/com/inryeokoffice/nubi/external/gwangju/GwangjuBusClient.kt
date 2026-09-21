package com.inryeokoffice.nubi.external.gwangju

import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.RouteStop

interface GwangjuBusClient {
    val dataSource: DataSource

    fun fetchRoutes(): List<BusRoute>

    fun fetchStops(): List<BusStop>

    fun fetchRouteStops(routeId: Long): List<Pair<RouteStop, BusStop>>

    fun fetchArrivals(stopId: Long): List<BusArrival>

    fun fetchPositions(routeId: Long): List<BusPosition>
}

class GwangjuExternalApiException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
