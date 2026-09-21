package com.inryeokoffice.nubi.api

import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.domain.NearbyBusStop
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NubiController::class)
class NubiControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var queryService: NubiQueryService

    @Test
    fun `nearby stops endpoint returns client-shaped response`() {
        Mockito.`when`(queryService.findNearbyStops(35.1595, 126.8526, 500, 20)).thenReturn(
            listOf(
                NearbyBusStop(
                    BusStop(2607, "1234", "광천치안센터", 126.8527, 35.1596),
                    15.2,
                ),
            ),
        )

        mockMvc
            .perform(
                get("/api/v1/stops/nearby")
                    .param("latitude", "35.1595")
                    .param("longitude", "126.8526"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.stops", hasSize<Any>(1)))
            .andExpect(jsonPath("$.stops[0].stop.stopId").value(2607))
            .andExpect(jsonPath("$.stops[0].distanceMeters").value(15.2))
    }

    @Test
    fun `nearby stops endpoint rejects an excessive radius`() {
        mockMvc
            .perform(
                get("/api/v1/stops/nearby")
                    .param("latitude", "35.1595")
                    .param("longitude", "126.8526")
                    .param("radiusMeters", "2001"),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }
}
