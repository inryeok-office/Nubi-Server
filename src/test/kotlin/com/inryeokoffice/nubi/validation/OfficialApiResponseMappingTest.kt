package com.inryeokoffice.nubi.validation

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OfficialApiResponseMappingTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `official list wrapper maps array items`() {
        val root =
            objectMapper.readTree(
                """
                {
                  "RESPONSE": {
                    "RESULT": {
                      "RESULT_CODE": "00",
                      "LINE_LIST": {
                        "ITEM": [
                          {"LINE_ID": 1, "LINE_NAME": "순환01A"},
                          {"LINE_ID": 2, "LINE_NAME": "지원15"}
                        ]
                      }
                    }
                  }
                }
                """.trimIndent(),
            )

        val items = root.items("LINE_LIST")

        assertThat(items).hasSize(2)
        assertThat(items.map { it.text("LINE_ID") }).containsExactly("1", "2")
    }

    @Test
    fun `official list wrapper maps a single object item`() {
        val root =
            objectMapper.readTree(
                """
                {"RESPONSE":{"RESULT":{"ARRIVE_LIST":{"ITEM":{"BUS_ID":"5280","LOW_BUS":"1"}}}}}
                """.trimIndent(),
            )

        val item = root.items("ARRIVE_LIST").single()

        assertThat(item.text("BUS_ID")).isEqualTo("5280")
        assertThat(item.text("LOW_BUS")).isEqualTo("1")
    }
}
