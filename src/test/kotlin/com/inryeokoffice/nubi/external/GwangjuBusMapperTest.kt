package com.inryeokoffice.nubi.external

import com.inryeokoffice.nubi.domain.DataSource
import com.inryeokoffice.nubi.domain.LowFloorStatus
import com.inryeokoffice.nubi.external.gwangju.GwangjuArrivalDto
import com.inryeokoffice.nubi.external.gwangju.GwangjuBusMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class GwangjuBusMapperTest {
    private val mapper = GwangjuBusMapper()

    @Test
    fun `arrival response maps observed identifiers and low floor state`() {
        val arrival =
            mapper.toArrival(
                GwangjuArrivalDto(
                    vehicleId = "775246",
                    routeId = "1",
                    routeName = "순환01A",
                    lowBus = "1",
                    remainingMinutes = "5",
                    remainingStops = "2",
                    currentStopId = "2420",
                ),
                DataSource.OBSERVED_WEB_API,
                Instant.parse("2026-09-21T00:00:00Z"),
            )

        assertThat(arrival).isNotNull
        assertThat(arrival!!.vehicleId).isEqualTo("775246")
        assertThat(arrival.lowFloorStatus).isEqualTo(LowFloorStatus.LOW_FLOOR)
        assertThat(arrival.remainingMinutes).isEqualTo(5)
        assertThat(arrival.dataSource).isEqualTo(DataSource.OBSERVED_WEB_API)
    }

    @Test
    fun `unknown LOW_BUS remains unknown while other fields are retained`() {
        val arrival =
            mapper.toArrival(
                GwangjuArrivalDto("vehicle", "1", "route", "unexpected", "-", "-", null),
                DataSource.OBSERVED_WEB_API,
                Instant.parse("2026-09-21T00:00:00Z"),
            )

        assertThat(arrival!!.lowFloorStatus).isEqualTo(LowFloorStatus.UNKNOWN)
        assertThat(arrival.remainingMinutes).isNull()
        assertThat(arrival.remainingStops).isNull()
    }
}
