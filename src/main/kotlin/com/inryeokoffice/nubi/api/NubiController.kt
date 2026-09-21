package com.inryeokoffice.nubi.api

import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.NearbyBusStop
import com.inryeokoffice.nubi.repository.RouteStopView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "NUBI public API", description = "광주 버스 정적 정보와 실시간 도착정보")
class NubiController(
    private val queryService: NubiQueryService,
) {
    @GetMapping("/stops/nearby")
    @Operation(summary = "주변 정류소 검색", description = "위도·경도 기준으로 거리순 주변 정류소를 검색합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "검색 성공"),
        ApiResponse(responseCode = "400", description = "좌표 또는 반경이 유효하지 않음"),
    )
    fun findNearbyStops(
        @Parameter(description = "위도", `in` = ParameterIn.QUERY, example = "35.1595")
        @RequestParam latitude: Double,
        @Parameter(description = "경도", `in` = ParameterIn.QUERY, example = "126.8526")
        @RequestParam longitude: Double,
        @Parameter(description = "검색 반경(m), 최대 2,000", `in` = ParameterIn.QUERY, example = "500")
        @RequestParam(defaultValue = "500") radiusMeters: Int,
        @Parameter(description = "최대 결과 수, 최대 100", `in` = ParameterIn.QUERY, example = "20")
        @RequestParam(defaultValue = "20") limit: Int,
    ): NearbyBusStopsResponse {
        validateCoordinates(latitude, longitude)
        if (radiusMeters !in 1..2_000) throw InvalidRequestException("radiusMeters must be between 1 and 2000")
        if (limit !in 1..100) throw InvalidRequestException("limit must be between 1 and 100")
        return NearbyBusStopsResponse(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            stops = queryService.findNearbyStops(latitude, longitude, radiusMeters, limit).map(::toNearbyResponse),
        )
    }

    @GetMapping("/stops/{stopId}")
    @Operation(summary = "정류소 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "정류소 조회 성공"),
        ApiResponse(responseCode = "404", description = "정류소 없음"),
    )
    fun findStop(
        @PathVariable stopId: Long,
    ): BusStopResponse = queryService.findStop(stopId).toResponse()

    @GetMapping("/stops/{stopId}/arrivals")
    @Operation(
        summary = "정류소 실시간 도착정보 조회",
        description = "조회 시점의 도착정보입니다. low-floor는 차량 응답의 관찰 상태이며 휠체어 탑승을 보장하지 않습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "도착정보 조회 성공"),
        ApiResponse(responseCode = "404", description = "정류소 없음"),
        ApiResponse(responseCode = "502", description = "광주 버스 데이터 원천 조회 실패"),
    )
    fun findArrivals(
        @PathVariable stopId: Long,
    ): ArrivalsResponse {
        val snapshot = queryService.findArrivals(stopId)
        return ArrivalsResponse(
            stopId = snapshot.stopId,
            dataSource = snapshot.dataSource,
            fetchedAt = snapshot.fetchedAt,
            arrivals = snapshot.arrivals.map(::toArrivalResponse),
        )
    }

    @GetMapping("/routes/{routeId}")
    @Operation(summary = "노선 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "노선 조회 성공"),
        ApiResponse(responseCode = "404", description = "노선 없음"),
    )
    fun findRoute(
        @PathVariable routeId: Long,
    ): BusRouteResponse = queryService.findRoute(routeId).toResponse()

    @GetMapping("/routes/{routeId}/stops")
    @Operation(summary = "노선 경유 정류소 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "경유 정류소 조회 성공"),
        ApiResponse(responseCode = "404", description = "노선 없음"),
    )
    fun findRouteStops(
        @PathVariable routeId: Long,
    ): RouteStopsResponse = RouteStopsResponse(routeId, queryService.findRouteStops(routeId).map(::toRouteStopResponse))

    private fun validateCoordinates(
        latitude: Double,
        longitude: Double,
    ) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            throw InvalidRequestException("latitude or longitude is out of range")
        }
    }

    private fun toNearbyResponse(nearby: NearbyBusStop): NearbyBusStopResponse =
        NearbyBusStopResponse(nearby.stop.toResponse(), nearby.distanceMeters)

    private fun toArrivalResponse(arrival: BusArrival): BusArrivalResponse =
        BusArrivalResponse(
            routeId = arrival.routeId,
            routeName = arrival.routeName,
            vehicleId = arrival.vehicleId,
            remainingMinutes = arrival.remainingMinutes,
            remainingStops = arrival.remainingStops,
            lowFloorStatus = arrival.lowFloorStatus.value,
            currentStopId = arrival.currentStopId,
            observedAt = arrival.observedAt,
        )

    private fun toRouteStopResponse(routeStop: RouteStopView): RouteStopResponse =
        RouteStopResponse(routeStop.routeStop.sequence, routeStop.stop.toResponse())

    private fun BusStop.toResponse(): BusStopResponse = BusStopResponse(stopId, arsId, name, latitude, longitude)

    private fun BusRoute.toResponse(): BusRouteResponse = BusRouteResponse(routeId, name, upwardDestination, downwardDestination, typeCode)
}
