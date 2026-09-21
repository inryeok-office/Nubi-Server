package com.inryeokoffice.nubi.domain

import java.time.Instant

data class BusPosition(
    val routeId: Long,
    val vehicleId: String?,
    val latitude: Double?,
    val longitude: Double?,
    val lowFloorStatus: LowFloorStatus,
    val observedAt: Instant,
    val dataSource: DataSource,
)
