package com.inryeokoffice.nubi.external.gwangju

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.inryeokoffice.nubi.config.GwangjuBusProperties
import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.BusRoute
import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.RouteStop
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Instant

@Component
class ObservedWebGwangjuBusClient(
    objectMapper: ObjectMapper,
    restClientBuilder: RestClient.Builder,
    properties: GwangjuBusProperties,
) : GwangjuBusClient {
    private val objectMapper = objectMapper
    private val restClient = restClientBuilder.baseUrl(properties.observedBaseUrl).build()
    private val mapper = GwangjuBusMapper()

    override val dataSource: DataSource = DataSource.OBSERVED_WEB_API

    override fun fetchRoutes(): List<BusRoute> =
        getList(
            "/busmap/lineSearchListTemp2",
            emptyMap(),
            object : TypeReference<List<GwangjuRouteDto>>() {},
        ).mapNotNull(mapper::toRoute)

    override fun fetchStops(): List<BusStop> =
        getList(
            "/busmap/stationSearchListTemp2",
            mapOf("BUSSTOP_NAME" to ""),
            object : TypeReference<List<GwangjuStopDto>>() {},
        ).mapNotNull(mapper::toStop)

    override fun fetchRouteStops(routeId: Long): List<Pair<RouteStop, BusStop>> =
        getList(
            "/busmap/lineStationListTemp2",
            mapOf("LINE_ID" to routeId.toString()),
            object : TypeReference<List<GwangjuStopDto>>() {},
        ).mapIndexedNotNull { index, dto -> mapper.toRouteStop(routeId, dto, index + 1) }

    override fun fetchArrivals(stopId: Long): List<BusArrival> {
        val observedAt = Instant.now()
        return getList(
            "/busmap/lineStationArriveInfoListTemp2",
            mapOf("BUSSTOP_ID" to stopId.toString()),
            object : TypeReference<List<GwangjuArrivalDto>>() {},
        ).mapNotNull { mapper.toArrival(it, dataSource, observedAt) }
    }

    override fun fetchPositions(routeId: Long): List<BusPosition> {
        val observedAt = Instant.now()
        return getList(
            "/busmap/lineBusLocationListTemp2",
            mapOf("LINE_ID" to routeId.toString()),
            object : TypeReference<List<GwangjuPositionDto>>() {},
        ).map { mapper.toPosition(routeId, it, dataSource, observedAt) }
    }

    private fun <T> getList(
        path: String,
        query: Map<String, String>,
        typeReference: TypeReference<List<T>>,
    ): List<T> {
        try {
            val body =
                restClient
                    .get()
                    .uri { builder ->
                        builder.path(path)
                        query.forEach { (key, value) -> builder.queryParam(key, value) }
                        builder.build()
                    }.retrieve()
                    .body(String::class.java)
                    ?: throw GwangjuExternalApiException("Gwangju API returned an empty response: $path")
            val listNode = objectMapper.readTree(body).path("list")
            if (!listNode.isArray) return emptyList()
            return objectMapper.readValue(listNode.toString(), typeReference)
        } catch (exception: GwangjuExternalApiException) {
            throw exception
        } catch (exception: RestClientException) {
            throw GwangjuExternalApiException("Gwangju API request failed: $path", exception)
        } catch (exception: Exception) {
            throw GwangjuExternalApiException("Gwangju API response could not be parsed: $path", exception)
        }
    }
}
