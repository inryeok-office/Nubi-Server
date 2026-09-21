package com.inryeokoffice.nubi.repository

import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.RouteStop
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

data class RouteStopView(
    val routeStop: RouteStop,
    val stop: BusStop,
)

@Repository
class RouteStopRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun upsert(routeStop: RouteStop) {
        jdbcTemplate.update(
            """
            INSERT INTO route_stop (route_id, stop_id, stop_sequence)
            VALUES (?, ?, ?)
            ON CONFLICT (route_id, stop_id) DO UPDATE SET stop_sequence = EXCLUDED.stop_sequence
            """.trimIndent(),
            routeStop.routeId,
            routeStop.stopId,
            routeStop.sequence,
        )
    }

    fun findByRouteId(routeId: Long): List<RouteStopView> =
        jdbcTemplate.query(
            """
            SELECT rs.route_id, rs.stop_id, rs.stop_sequence,
                   s.ars_id, s.name, ST_X(s.location) AS longitude, ST_Y(s.location) AS latitude
            FROM route_stop rs
            JOIN bus_stop s ON s.stop_id = rs.stop_id
            WHERE rs.route_id = ?
            ORDER BY rs.stop_sequence
            """.trimIndent(),
            { resultSet, _ ->
                RouteStopView(
                    routeStop =
                        RouteStop(
                            routeId = resultSet.getLong("route_id"),
                            stopId = resultSet.getLong("stop_id"),
                            sequence = resultSet.getInt("stop_sequence"),
                        ),
                    stop =
                        BusStop(
                            stopId = resultSet.getLong("stop_id"),
                            arsId = resultSet.getString("ars_id"),
                            name = resultSet.getString("name"),
                            longitude = resultSet.getDouble("longitude"),
                            latitude = resultSet.getDouble("latitude"),
                        ),
                )
            },
            routeId,
        )
}
