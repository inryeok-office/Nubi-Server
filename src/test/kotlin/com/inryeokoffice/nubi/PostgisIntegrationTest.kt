package com.inryeokoffice.nubi

import org.assertj.core.api.Assertions.assertThat
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
