package com.inryeokoffice.nubi.domain

data class BusStop(
    val stopId: Long,
    val arsId: String?,
    val name: String,
    val longitude: Double,
    val latitude: Double,
)

data class NearbyBusStop(
    val stop: BusStop,
    val distanceMeters: Double,
)
