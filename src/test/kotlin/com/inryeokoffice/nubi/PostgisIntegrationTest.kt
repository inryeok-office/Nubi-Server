package com.inryeokoffice.nubi

import com.inryeokoffice.nubi.domain.BusStop
import com.inryeokoffice.nubi.repository.BusStopRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PostgisIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var busStopRepository: BusStopRepository

    @BeforeEach
    fun cleanStaticData() {
        jdbcTemplate.update("DELETE FROM bus_stop")
    }

    @Test
    fun `flyway migration enables postgis`() {
        val postgisVersion = jdbcTemplate.queryForObject("SELECT PostGIS_Version()", String::class.java)
        val metadataValue =
            jdbcTemplate.queryForObject(
                "SELECT metadata_value FROM app_metadata WHERE metadata_key = 'schema-purpose'",
                String::class.java,
            )

        assertThat(postgisVersion).isNotBlank()
        assertThat(metadataValue).isEqualTo("NUBI technical PoC")
    }

    @Test
    fun `nearby stop search uses PostGIS distance ordering`() {
        busStopRepository.upsert(BusStop(1, "1001", "near", 126.8526, 35.1595))
        busStopRepository.upsert(BusStop(2, "1002", "nearer", 126.8527, 35.1595))
        busStopRepository.upsert(BusStop(3, "1003", "far", 127.0, 35.1595))

        val nearby = busStopRepository.findNearby(35.1595, 126.8526, 500, 10)

        assertThat(nearby.map { it.stop.stopId }).containsExactly(1, 2)
        assertThat(nearby[0].distanceMeters).isLessThan(nearby[1].distanceMeters)
    }

    companion object {
        private val postgresImage =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")

        @Container
        @JvmStatic
        val postgres =
            PostgreSQLContainer(postgresImage)
                .withDatabaseName("nubi")
                .withUsername("nubi")
                .withPassword("nubi")

        @JvmStatic
        @DynamicPropertySource
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
