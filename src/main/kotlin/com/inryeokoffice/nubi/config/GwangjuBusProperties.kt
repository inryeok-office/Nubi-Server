package com.inryeokoffice.nubi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("nubi.gwangju")
data class GwangjuBusProperties(
    val dataSource: String = "observed-web-api",
    val apiKey: String = "",
    val observedBaseUrl: String = "https://bus.gwangju.go.kr",
    val officialBaseUrl: String = "",
    val collectionEnabled: Boolean = false,
)
