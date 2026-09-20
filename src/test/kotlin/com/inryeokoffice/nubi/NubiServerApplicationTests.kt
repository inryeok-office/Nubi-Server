package com.inryeokoffice.nubi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    properties = [
        "spring.flyway.enabled=false",
        "management.health.db.enabled=false",
    ],
)
@ActiveProfiles("test")
class NubiServerApplicationTests {
    @Test
    fun contextLoads() {
    }
}
