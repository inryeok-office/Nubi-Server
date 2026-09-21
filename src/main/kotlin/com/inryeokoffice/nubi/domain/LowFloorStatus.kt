package com.inryeokoffice.nubi.domain

enum class LowFloorStatus(
    val value: String,
) {
    LOW_FLOOR("low-floor"),
    STANDARD("standard"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromExternalCode(raw: String?): LowFloorStatus =
            when (raw?.trim()) {
                "1" -> LOW_FLOOR
                "0" -> STANDARD
                else -> UNKNOWN
            }
    }
}
