package com.inryeokoffice.nubi.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.inryeokoffice.nubi.config.GwangjuBusProperties
import com.inryeokoffice.nubi.domain.BusArrival
import com.inryeokoffice.nubi.domain.BusPosition
import com.inryeokoffice.nubi.domain.LowFloorStatus
import com.inryeokoffice.nubi.external.gwangju.ObservedWebGwangjuBusClient
import org.springframework.web.client.RestClient
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.system.exitProcess

private const val DEFAULT_OFFICIAL_BASE_URL = "https://apis.data.go.kr/6290000/gj_bis"
private const val DEFAULT_OBSERVED_BASE_URL = "https://bus.gwangju.go.kr"
private val KNOWN_ERROR_CODES = setOf("01", "04", "05", "10", "12", "20", "22", "23", "29", "30", "31")

private data class OfficialCall(
    val path: String,
    val collectionKey: String,
    val httpStatus: Int,
    val body: JsonNode?,
    val resultCode: String?,
    val resultMessage: String?,
    val items: List<JsonNode>,
    val transportError: String? = null,
)

private data class NormalizedArrival(
    val vehicleId: String,
    val routeId: String?,
    val lowBus: String?,
    val remainingMinutes: String?,
    val remainingStops: String?,
    val currentStopId: String?,
)

private data class NormalizedPosition(
    val vehicleId: String,
    val routeId: String?,
    val lowBus: String?,
    val currentStopId: String?,
    val sequence: String?,
)

private data class ObservedSnapshot(
    val arrivals: List<BusArrival>,
    val positions: List<BusPosition>,
    val rawPositions: List<JsonNode>,
)

private data class ValidationOutcome(
    val exitCode: Int,
    val report: String,
    val consoleMessage: String,
)

private class OfficialGwangjuApiClient(
    private val apiKey: String,
    private val baseUrl: String,
    private val objectMapper: ObjectMapper,
) {
    private val httpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

    fun get(
        path: String,
        collectionKey: String,
        parameters: Map<String, String>,
    ): OfficialCall {
        val query =
            linkedMapOf("serviceKey" to apiKey, "resultType" to "json")
                .apply { putAll(parameters) }
                .entries
                .joinToString("&") { (key, value) ->
                    "${encode(key)}=${encode(value)}"
                }
        return try {
            val response =
                httpClient.send(
                    HttpRequest
                        .newBuilder()
                        .uri(URI.create("$baseUrl$path?$query"))
                        .timeout(Duration.ofSeconds(15))
                        .header("Accept", "application/json")
                        .GET()
                        .build(),
                    HttpResponse.BodyHandlers.ofString(UTF_8),
                )
            val body = runCatching { objectMapper.readTree(response.body()) }.getOrNull()
            val result = body?.path("RESPONSE")?.path("RESULT")
            OfficialCall(
                path = path,
                collectionKey = collectionKey,
                httpStatus = response.statusCode(),
                body = body,
                resultCode = result?.text("RESULT_CODE"),
                resultMessage = result?.text("RESULT_MSG"),
                items = body.items(collectionKey),
            )
        } catch (exception: Exception) {
            OfficialCall(
                path = path,
                collectionKey = collectionKey,
                httpStatus = 0,
                body = null,
                resultCode = null,
                resultMessage = null,
                items = emptyList(),
                transportError = exception::class.simpleName,
            )
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, UTF_8)
}

private class ObservedRawClient(
    baseUrl: String,
    private val objectMapper: ObjectMapper,
) {
    private val restClient = RestClient.builder().baseUrl(baseUrl).build()

    fun fetchPositions(routeId: String): List<JsonNode> {
        val body =
            restClient
                .get()
                .uri { builder -> builder.path("/busmap/lineBusLocationListTemp2").queryParam("LINE_ID", routeId).build() }
                .retrieve()
                .body(String::class.java)
                ?: return emptyList()
        val list = objectMapper.readTree(body).path("list")
        return if (list.isArray) list.toList() else emptyList()
    }
}

internal fun JsonNode?.text(field: String): String? =
    this
        ?.path(field)
        ?.takeUnless { it.isMissingNode || it.isNull }
        ?.asText()
        ?.trim()
        ?.ifBlank { null }

internal fun JsonNode?.items(collectionKey: String): List<JsonNode> {
    val collection = this?.path("RESPONSE")?.path("RESULT")?.path(collectionKey) ?: return emptyList()
    val item = collection.path("ITEM")
    return when {
        item.isArray -> item.toList()
        item.isObject -> listOf(item)
        else -> emptyList()
    }
}

private fun JsonNode.fieldNamesSet(): Set<String> = if (!isObject) emptySet() else fieldNames().asSequence().toSet()

private fun JsonNode.fieldNamesInItems(): Set<String> = if (!isArray) emptySet() else flatMap { it.fieldNamesSet() }.toSet()

private fun field(
    node: JsonNode,
    name: String,
): String? = node.text(name)

private fun String?.normalized(): String? = this?.trim()?.ifBlank { null }

private fun String?.asLowFloorStatus(): LowFloorStatus =
    when (this.normalized()) {
        "1" -> LowFloorStatus.LOW_FLOOR
        "0" -> LowFloorStatus.STANDARD
        else -> LowFloorStatus.UNKNOWN
    }

private fun LowFloorStatus.asExternalCode(): String? =
    when (this) {
        LowFloorStatus.LOW_FLOOR -> "1"
        LowFloorStatus.STANDARD -> "0"
        LowFloorStatus.UNKNOWN -> null
    }

private fun normalizeOfficialArrivals(calls: List<OfficialCall>): List<NormalizedArrival> =
    calls
        .flatMap { it.items }
        .mapNotNull { item ->
            field(item, "BUS_ID")?.let { vehicleId ->
                NormalizedArrival(
                    vehicleId = vehicleId,
                    routeId = field(item, "LINE_ID"),
                    lowBus = field(item, "LOW_BUS"),
                    remainingMinutes = field(item, "REMAIN_MIN"),
                    remainingStops = field(item, "REMAIN_STOP"),
                    currentStopId = field(item, "CURR_STOP_ID"),
                )
            }
        }

private fun normalizeOfficialPositions(call: OfficialCall): List<NormalizedPosition> =
    call.items.mapNotNull { item ->
        field(item, "BUS_ID")?.let { vehicleId ->
            NormalizedPosition(
                vehicleId = vehicleId,
                routeId = field(item, "LINE_ID"),
                lowBus = field(item, "LOW_BUS"),
                currentStopId = field(item, "CURR_STOP_ID"),
                sequence = field(item, "SEQ"),
            )
        }
    }

private fun normalizeObservedArrivals(arrivals: List<BusArrival>): List<NormalizedArrival> =
    arrivals.mapNotNull { arrival ->
        arrival.vehicleId?.let { vehicleId ->
            NormalizedArrival(
                vehicleId = vehicleId,
                routeId = arrival.routeId.toString(),
                lowBus = arrival.lowFloorStatus.asExternalCode(),
                remainingMinutes = arrival.remainingMinutes?.toString(),
                remainingStops = arrival.remainingStops?.toString(),
                currentStopId = arrival.currentStopId?.toString(),
            )
        }
    }

private fun normalizeObservedPositions(
    routeId: String,
    positions: List<BusPosition>,
): List<NormalizedPosition> =
    positions.mapNotNull { position ->
        position.vehicleId?.let { vehicleId ->
            NormalizedPosition(
                vehicleId = vehicleId,
                routeId = routeId,
                lowBus = position.lowFloorStatus.asExternalCode(),
                currentStopId = null,
                sequence = null,
            )
        }
    }

private fun sanitize(
    value: String?,
    secret: String,
): String? {
    if (value == null) return null
    val encodedSecret = URLEncoder.encode(secret, UTF_8)
    return value
        .replace(secret, "[REDACTED]")
        .replace(encodedSecret, "[REDACTED]")
        .replace(Regex("[\\r\\n]+"), " ")
        .take(500)
}

private fun isAuthenticationFailure(call: OfficialCall): Boolean =
    call.httpStatus !in 200..299 ||
        call.body == null ||
        call.transportError != null ||
        call.resultCode?.uppercase(Locale.ROOT) in KNOWN_ERROR_CODES

private fun describeCall(
    call: OfficialCall,
    secret: String,
): String {
    val fields =
        call.items
            .flatMap(JsonNode::fieldNamesSet)
            .toSet()
            .sorted()
            .joinToString(", ")
    return listOf(
        "- ${call.path}: http=${call.httpStatus}",
        "  resultCode=${sanitize(call.resultCode, secret) ?: "-"}",
        "  resultMessage=${sanitize(call.resultMessage, secret) ?: "-"}",
        "  collection=${call.collectionKey}, itemCount=${call.items.size}",
        "  fields=${if (fields.isBlank()) "-" else fields}",
        "  transportError=${call.transportError ?: "-"}",
    ).joinToString("\n")
}

private fun compareRows(
    official: List<NormalizedArrival>,
    observed: List<NormalizedArrival>,
): String {
    val officialByVehicle = official.associateBy { it.vehicleId }
    val observedByVehicle = observed.associateBy { it.vehicleId }
    val overlap = officialByVehicle.keys.intersect(observedByVehicle.keys)
    val lowBusMatches = overlap.count { vehicleId -> officialByVehicle[vehicleId]?.lowBus == observedByVehicle[vehicleId]?.lowBus }
    return "official=${official.size}, observed=${observed.size}, vehicleOverlap=${overlap.size}, lowBusMatches=$lowBusMatches"
}

private fun executeValidation(apiKey: String): ValidationOutcome {
    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    val officialBaseUrl = System.getenv("GWANGJU_BUS_OFFICIAL_BASE_URL").normalized() ?: DEFAULT_OFFICIAL_BASE_URL
    val observedBaseUrl = System.getenv("GWANGJU_BUS_OBSERVED_BASE_URL").normalized() ?: DEFAULT_OBSERVED_BASE_URL
    val official = OfficialGwangjuApiClient(apiKey, officialBaseUrl, objectMapper)
    val report = StringBuilder()
    val startedAt = Instant.now()
    report.appendLine("# 광주버스 정식 OpenAPI 검증 결과")
    report.appendLine()
    report.appendLine("- 검증 시작: $startedAt")
    report.appendLine("- API Key: configured (값 미기록)")
    report.appendLine("- official base: ${sanitize(officialBaseUrl, apiKey)}")
    report.appendLine("- observed base: $observedBaseUrl")
    report.appendLine("- 호출 정책: lineInfo 1회, stationInfo 1회, lineStationInfo 1회, 정류소 최대 3개, busLocationInfo 1회")
    report.appendLine()

    val lineInfo = official.get("/lineInfo", "LINE_LIST", emptyMap())
    report.appendLine("## 공식 endpoint 호출")
    report.appendLine(describeCall(lineInfo, apiKey))
    report.appendLine()

    if (isAuthenticationFailure(lineInfo)) {
        report.appendLine("## 판정")
        report.appendLine("인증 실패 또는 공식 응답 구조 확인 실패로 후속 호출을 중단했다.")
        return ValidationOutcome(
            exitCode = 2,
            report = report.toString(),
            consoleMessage = "Official API authentication or initial response validation failed. No further API calls were made.",
        )
    }

    val routeItem = lineInfo.items.firstOrNull { field(it, "LINE_ID") == "1" } ?: lineInfo.items.firstOrNull()
    val routeId = routeItem?.let { field(it, "LINE_ID") }
    if (routeId == null) {
        report.appendLine("## 판정")
        report.appendLine("성공 응답에 사용할 LINE_ID가 없어 후속 호출을 중단했다.")
        return ValidationOutcome(2, report.toString(), "Official lineInfo returned no usable LINE_ID. No further API calls were made.")
    }

    val stationInfo = official.get("/stationInfo", "STATION_LIST", emptyMap())
    val lineStationInfo = official.get("/lineStationInfo", "BUSSTOP_LIST", mapOf("LINE_ID" to routeId))
    report.appendLine(describeCall(stationInfo, apiKey))
    report.appendLine(describeCall(lineStationInfo, apiKey))
    report.appendLine()

    val stopIds =
        lineStationInfo.items
            .mapNotNull { field(it, "BUSSTOP_ID") }
            .distinct()
            .take(3)
            .ifEmpty {
                stationInfo.items
                    .mapNotNull { field(it, "BUSSTOP_ID") }
                    .distinct()
                    .take(3)
            }
    report.appendLine("- selectedLineId=$routeId")
    report.appendLine("- selectedStopIds=${if (stopIds.isEmpty()) "-" else stopIds.joinToString(", ")}")

    val arrivalCalls = stopIds.map { stopId -> official.get("/arriveInfo", "ARRIVE_LIST", mapOf("BUSSTOP_ID" to stopId)) }
    arrivalCalls.forEach { report.appendLine(describeCall(it, apiKey)) }
    val positionCall = official.get("/busLocationInfo", "BUSLOCATION_LIST", mapOf("LINE_ID" to routeId))
    report.appendLine(describeCall(positionCall, apiKey))
    report.appendLine()

    val officialArrivals = normalizeOfficialArrivals(arrivalCalls)
    val officialPositions = normalizeOfficialPositions(positionCall)
    val officialArrivalFields = arrivalCalls.flatMap { it.items }.flatMap(JsonNode::fieldNamesSet).toSet()
    val officialPositionFields = positionCall.items.flatMap(JsonNode::fieldNamesSet).toSet()
    report.appendLine("## 공식 실제 필드 검증")
    report.appendLine(
        "- arriveInfo core fields: BUS_ID=${"BUS_ID" in officialArrivalFields}, LOW_BUS=${"LOW_BUS" in officialArrivalFields}, LINE_ID=${"LINE_ID" in officialArrivalFields}, REMAIN_MIN=${"REMAIN_MIN" in officialArrivalFields}, REMAIN_STOP=${"REMAIN_STOP" in officialArrivalFields}, CURR_STOP_ID=${"CURR_STOP_ID" in officialArrivalFields}",
    )
    report.appendLine(
        "- busLocationInfo core fields: BUS_ID=${"BUS_ID" in officialPositionFields}, LINE_ID=${"LINE_ID" in officialPositionFields}, LOW_BUS=${"LOW_BUS" in officialPositionFields}, CURR_STOP_ID=${"CURR_STOP_ID" in officialPositionFields}, CARNO=${"CARNO" in officialPositionFields}, SEQ=${"SEQ" in officialPositionFields}",
    )
    report.appendLine("- official location coordinates documented/observed: false unless present in fields above")
    report.appendLine()

    var observedSnapshot: ObservedSnapshot? = null
    var observedError: String? = null
    try {
        val observedMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val observedClient =
            ObservedWebGwangjuBusClient(
                observedMapper,
                RestClient.builder(),
                GwangjuBusProperties(observedBaseUrl = observedBaseUrl),
            )
        val arrivals = stopIds.flatMap { observedClient.fetchArrivals(it.toLong()) }
        val positions = observedClient.fetchPositions(routeId.toLong())
        val rawPositions = ObservedRawClient(observedBaseUrl, observedMapper).fetchPositions(routeId)
        observedSnapshot = ObservedSnapshot(arrivals, positions, rawPositions)
    } catch (exception: Exception) {
        observedError = exception::class.simpleName ?: "UnknownException"
    }

    report.appendLine("## official vs observed 비교")
    if (observedSnapshot == null) {
        report.appendLine("- observed comparison unavailable: $observedError")
    } else {
        val observedArrivals = normalizeObservedArrivals(observedSnapshot.arrivals)
        val observedPositions = normalizeObservedPositions(routeId, observedSnapshot.positions)
        report.appendLine("- arrivals: ${compareRows(officialArrivals, observedArrivals)}")
        val officialPositionRows =
            officialPositions.map { row ->
                NormalizedArrival(row.vehicleId, row.routeId, row.lowBus, null, null, row.currentStopId)
            }
        val observedPositionRows =
            observedPositions.map { row ->
                NormalizedArrival(row.vehicleId, row.routeId, row.lowBus, null, null, row.currentStopId)
            }
        report.appendLine("- positions: ${compareRows(officialPositionRows, observedPositionRows)}")
        report.appendLine(
            "- observed raw position sequence fields: ${observedSnapshot.rawPositions.flatMap(
                JsonNode::fieldNamesSet,
            ).filter { it.contains("SEQ") }.distinct().sorted().ifEmpty { listOf("-") }.joinToString(", ")}",
        )
        report.appendLine("- official position sequence field: ${if ("SEQ" in officialPositionFields) "SEQ" else "missing"}")
    }
    report.appendLine()

    val officialCoreAvailable =
        officialArrivals.isNotEmpty() &&
            officialPositions.isNotEmpty() &&
            officialArrivalFields.containsAll(setOf("BUS_ID", "LINE_ID", "LOW_BUS")) &&
            officialPositionFields.containsAll(setOf("BUS_ID", "LINE_ID", "LOW_BUS"))
    val observedArrivals = observedSnapshot?.let { normalizeObservedArrivals(it.arrivals) }.orEmpty()
    val arrivalOverlap = officialArrivals.map { it.vehicleId }.intersect(observedArrivals.map { it.vehicleId }.toSet()).size
    val verdict =
        when {
            !officialCoreAvailable -> "C"
            observedSnapshot == null || arrivalOverlap == 0 -> "A2"
            else -> "A1"
        }
    report.appendLine("## 최종 판정")
    report.appendLine("$verdict")
    report.appendLine("- official arrival vehicle rows=${officialArrivals.size}")
    report.appendLine("- official position vehicle rows=${officialPositions.size}")
    report.appendLine("- arrival vehicle overlap with observed=$arrivalOverlap")
    report.appendLine("- 좌표 기반 위치 비교는 공식 스키마에 좌표가 없어 수행하지 않았다.")
    report.appendLine("- LOW_BUS 공식 위치 스키마 정의: 0=일반, 1=저상; 도착 응답의 실제 의미는 위 비교 결과와 함께 확인 필요")

    return ValidationOutcome(0, report.toString(), "Official Gwangju bus API validation completed. Verdict=$verdict")
}

private fun writeReport(
    report: String,
    apiKey: String,
): Path {
    val reportPath = Path.of("build", "reports", "gwangju-bus-validation", "official-api-validation-result.md")
    val encodedKey = URLEncoder.encode(apiKey, UTF_8)
    if (report.contains(apiKey) || report.contains(encodedKey)) {
        Files.deleteIfExists(reportPath)
        throw IllegalStateException("Validation report safety check failed; report was not retained")
    }
    reportPath.parent.createDirectories()
    reportPath.writeText(report)
    return reportPath
}

fun main() {
    val apiKey = System.getenv("GWANGJU_BUS_API_KEY")?.trim()
    if (apiKey.isNullOrEmpty()) {
        println("GWANGJU_BUS_API_KEY: missing")
        println("Add it to the local .env file and run ./gradlew validateGwangjuBusOfficialApi again.")
        exitProcess(2)
    }

    val outcome =
        runCatching { executeValidation(apiKey) }
            .getOrElse { exception ->
                ValidationOutcome(
                    exitCode = 2,
                    report = "# 광주버스 정식 OpenAPI 검증 결과\n\n실행 실패 유형: ${exception::class.simpleName}\n",
                    consoleMessage = "Validation failed before completion. Secret values were not printed.",
                )
            }
    val reportPath =
        runCatching { writeReport(outcome.report, apiKey) }
            .getOrElse {
                println("Validation report safety check failed. No report was retained.")
                exitProcess(2)
            }
    println(outcome.consoleMessage)
    println("Report: $reportPath")
    if (outcome.exitCode != 0) exitProcess(outcome.exitCode)
}
