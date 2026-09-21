package com.inryeokoffice.nubi.domain

import java.time.Instant

data class BusArrival(
    val routeId: Long,
    val routeName: String?,
    val vehicleId: String?,
    val remainingMinutes: Int?,
    val remainingStops: Int?,
    val lowFloorStatus: LowFloorStatus,
    val currentStopId: Long?,
    val observedAt: Instant,
    val dataSource: DataSource,
)
