package com.inryeokoffice.nubi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class NubiServerApplication

fun main(args: Array<String>) {
    runApplication<NubiServerApplication>(*args)
}
