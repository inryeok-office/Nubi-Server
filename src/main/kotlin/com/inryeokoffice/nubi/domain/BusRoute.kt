package com.inryeokoffice.nubi.domain

data class BusRoute(
    val routeId: Long,
    val name: String,
    val upwardDestination: String?,
    val downwardDestination: String?,
    val typeCode: String?,
)
