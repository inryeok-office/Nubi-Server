package com.inryeokoffice.nubi.repository

import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.NearbyBusStop
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BusStopRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun upsert(stop: BusStop) {
        jdbcTemplate.update(
            """
            INSERT INTO bus_stop (stop_id, ars_id, name, location, updated_at)
            VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), CURRENT_TIMESTAMP)
            ON CONFLICT (stop_id) DO UPDATE SET
                ars_id = EXCLUDED.ars_id,
                name = EXCLUDED.name,
                location = EXCLUDED.location,
                updated_at = CURRENT_TIMESTAMP
            """.trimIndent(),
            stop.stopId,
            stop.arsId,
            stop.name,
            stop.longitude,
            stop.latitude,
        )
    }

    fun findById(stopId: Long): BusStop? =
        jdbcTemplate
            .query(
                """
                SELECT stop_id, ars_id, name, ST_X(location) AS longitude, ST_Y(location) AS latitude
                FROM bus_stop
                WHERE stop_id = ?
                """.trimIndent(),
                { resultSet, _ -> resultSet.toBusStop() },
                stopId,
            ).singleOrNull()

    fun findNearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        limit: Int,
    ): List<NearbyBusStop> {
        val point = "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography"
        val sql =
            """
            SELECT stop_id, ars_id, name, ST_X(location) AS longitude, ST_Y(location) AS latitude,
                   ST_Distance(location::geography, $point) AS distance_meters
            FROM bus_stop
            WHERE ST_DWithin(location::geography, $point, :radiusMeters)
            ORDER BY distance_meters, stop_id
            LIMIT :limit
            """.trimIndent()
        val parameters =
            MapSqlParameterSource()
                .addValue("latitude", latitude)
                .addValue("longitude", longitude)
                .addValue("radiusMeters", radiusMeters)
                .addValue("limit", limit)
        return namedParameterJdbcTemplate.query(sql, parameters) { resultSet, _ ->
            NearbyBusStop(resultSet.toBusStop(), resultSet.getDouble("distance_meters"))
        }
    }

    private fun java.sql.ResultSet.toBusStop(): BusStop =
        BusStop(
            stopId = getLong("stop_id"),
            arsId = getString("ars_id"),
            name = getString("name"),
            longitude = getDouble("longitude"),
            latitude = getDouble("latitude"),
        )
}
