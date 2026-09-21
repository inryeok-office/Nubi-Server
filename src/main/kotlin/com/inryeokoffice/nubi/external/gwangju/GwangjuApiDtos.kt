package com.inryeokoffice.nubi.external.gwangju

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GwangjuRouteDto(
    @JsonProperty("LINE_ID") val routeId: String?,
    @JsonProperty("LINE_NAME") val name: String?,
    @JsonProperty("DIR_UP_NAME") val upwardDestination: String?,
    @JsonProperty("DIR_DOWN_NAME") val downwardDestination: String?,
    @JsonProperty("LINE_KIND") val typeCode: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GwangjuStopDto(
    @JsonProperty("BUSSTOP_ID") val stopId: String?,
    @JsonProperty("ARS_ID") val arsId: String?,
    @JsonProperty("BUSSTOP_NAME") val name: String?,
    @JsonProperty("BUSSTOP_NM") val displayName: String?,
    @JsonProperty("LONGITUDE") val longitude: String?,
    @JsonProperty("LATITUDE") val latitude: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GwangjuArrivalDto(
    @JsonProperty("BUS_ID") val vehicleId: String?,
    @JsonProperty("LINE_ID") val routeId: String?,
    @JsonProperty("LINE_NAME") val routeName: String?,
    @JsonProperty("LOW_BUS") val lowBus: String?,
    @JsonProperty("REMAIN_MIN") val remainingMinutes: String?,
    @JsonProperty("REMAIN_STOP") val remainingStops: String?,
    @JsonProperty("CURR_STOP_ID") val currentStopId: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GwangjuPositionDto(
    @JsonProperty("BUS_ID") val vehicleId: String?,
    @JsonProperty("LOW_BUS") val lowBus: String?,
    @JsonProperty("LONGITUDE") val longitude: String?,
    @JsonProperty("LATITUDE") val latitude: String?,
)
