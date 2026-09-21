package com.inryeokoffice.nubi.external.gwangju

import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.LowFloorStatus
import com.inryeokoffice.nubi.domain.RouteStop
import java.time.Instant

class GwangjuBusMapper {
    fun toRoute(dto: GwangjuRouteDto): BusRoute? {
        val routeId = dto.routeId?.toLongOrNull() ?: return null
        val name = dto.name?.trim().orEmpty()
        if (name.isBlank()) return null
        return BusRoute(routeId, name, dto.upwardDestination, dto.downwardDestination, dto.typeCode)
    }

    fun toStop(dto: GwangjuStopDto): BusStop? {
        val stopId = dto.stopId?.toLongOrNull() ?: return null
        val longitude = dto.longitude?.toDoubleOrNull() ?: return null
        val latitude = dto.latitude?.toDoubleOrNull() ?: return null
        if (longitude !in -180.0..180.0 || latitude !in -90.0..90.0) return null
        val name =
            dto.name
                ?.trim()
                .orEmpty()
                .ifBlank { dto.displayName?.trim().orEmpty() }
        if (name.isBlank()) return null
        return BusStop(stopId, dto.arsId?.trim()?.ifBlank { null }, name, longitude, latitude)
    }

    fun toRouteStop(
        routeId: Long,
        dto: GwangjuStopDto,
        sequence: Int,
    ): Pair<RouteStop, BusStop>? {
        val stop = toStop(dto) ?: return null
        return RouteStop(routeId, stop.stopId, sequence) to stop
    }

    fun toArrival(
        dto: GwangjuArrivalDto,
        source: DataSource,
        observedAt: Instant,
    ): BusArrival? {
        val routeId = dto.routeId?.toLongOrNull() ?: return null
        return BusArrival(
            routeId = routeId,
            routeName = dto.routeName?.trim()?.ifBlank { null },
            vehicleId = dto.vehicleId?.trim()?.ifBlank { null },
            remainingMinutes = dto.remainingMinutes?.toIntOrNull(),
            remainingStops = dto.remainingStops?.toIntOrNull(),
            lowFloorStatus = LowFloorStatus.fromExternalCode(dto.lowBus),
            currentStopId = dto.currentStopId?.toLongOrNull(),
            observedAt = observedAt,
            dataSource = source,
        )
    }

    fun toPosition(
        routeId: Long,
        dto: GwangjuPositionDto,
        source: DataSource,
        observedAt: Instant,
    ): BusPosition =
        BusPosition(
            routeId = routeId,
            vehicleId = dto.vehicleId?.trim()?.ifBlank { null },
            latitude = dto.latitude?.toDoubleOrNull(),
            longitude = dto.longitude?.toDoubleOrNull(),
            lowFloorStatus = LowFloorStatus.fromExternalCode(dto.lowBus),
            observedAt = observedAt,
            dataSource = source,
        )
}
