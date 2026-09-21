package com.inryeokoffice.nubi

import com.inryeokoffice.nubi.domain.LowFloorStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LowFloorStatusTest {
    @Test
    fun `known LOW_BUS codes map to meaningful states`() {
        assertThat(LowFloorStatus.fromExternalCode("1")).isEqualTo(LowFloorStatus.LOW_FLOOR)
        assertThat(LowFloorStatus.fromExternalCode("0")).isEqualTo(LowFloorStatus.STANDARD)
    }

    @Test
    fun `unknown or malformed LOW_BUS codes do not break mapping`() {
        assertThat(LowFloorStatus.fromExternalCode("99")).isEqualTo(LowFloorStatus.UNKNOWN)
        assertThat(LowFloorStatus.fromExternalCode("not-a-code")).isEqualTo(LowFloorStatus.UNKNOWN)
        assertThat(LowFloorStatus.fromExternalCode(null)).isEqualTo(LowFloorStatus.UNKNOWN)
    }
}
