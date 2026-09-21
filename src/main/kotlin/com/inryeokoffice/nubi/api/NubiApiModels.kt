package com.inryeokoffice.nubi.api

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class BusStopResponse(
    val stopId: Long,
    val arsId: String?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class NearbyBusStopResponse(
    val stop: BusStopResponse,
    val distanceMeters: Double,
)

data class NearbyBusStopsResponse(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val stops: List<NearbyBusStopResponse>,
)

data class BusArrivalResponse(
    val routeId: Long,
    val routeName: String?,
    val vehicleId: String?,
    val remainingMinutes: Int?,
    val remainingStops: Int?,
    @field:Schema(
        description = "관찰된 차량의 저상버스 상태. 탑승 가능을 보장하지 않음.",
        allowableValues = ["low-floor", "standard", "unknown"],
        example = "low-floor",
    )
    val lowFloorStatus: String,
    val currentStopId: Long?,
    val observedAt: Instant,
)

data class ArrivalsResponse(
    val stopId: Long,
    @field:Schema(description = "데이터 출처: official-open-api 또는 observed-web-api")
    val dataSource: String,
    val fetchedAt: Instant,
    val arrivals: List<BusArrivalResponse>,
)

data class BusRouteResponse(
    val routeId: Long,
    val name: String,
    val upwardDestination: String?,
    val downwardDestination: String?,
    val typeCode: String?,
)

data class RouteStopsResponse(
    val routeId: Long,
    val stops: List<RouteStopResponse>,
)

data class RouteStopResponse(
    val sequence: Int,
    val stop: BusStopResponse,
)
