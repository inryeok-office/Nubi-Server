package com.inryeokoffice.nubi.repository

import com.inryeokoffice.nubi.domain.BusRoute
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BusRouteRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun upsert(route: BusRoute) {
        jdbcTemplate.update(
            """
            INSERT INTO bus_route (route_id, name, upward_destination, downward_destination, type_code, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (route_id) DO UPDATE SET
                name = EXCLUDED.name,
                upward_destination = EXCLUDED.upward_destination,
                downward_destination = EXCLUDED.downward_destination,
                type_code = EXCLUDED.type_code,
                updated_at = CURRENT_TIMESTAMP
            """.trimIndent(),
            route.routeId,
            route.name,
            route.upwardDestination,
            route.downwardDestination,
            route.typeCode,
        )
    }

    fun findById(routeId: Long): BusRoute? =
        jdbcTemplate
            .query(
                """
                SELECT route_id, name, upward_destination, downward_destination, type_code
                FROM bus_route
                WHERE route_id = ?
                """.trimIndent(),
                { resultSet, _ ->
                    BusRoute(
                        routeId = resultSet.getLong("route_id"),
                        name = resultSet.getString("name"),
                        upwardDestination = resultSet.getString("upward_destination"),
                        downwardDestination = resultSet.getString("downward_destination"),
                        typeCode = resultSet.getString("type_code"),
                    )
                },
                routeId,
            ).singleOrNull()
}
