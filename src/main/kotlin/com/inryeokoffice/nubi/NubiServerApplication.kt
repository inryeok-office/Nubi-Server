package com.inryeokoffice.nubi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NubiServerApplication

fun main(args: Array<String>) {
    runApplication<NubiServerApplication>(*args)
}
