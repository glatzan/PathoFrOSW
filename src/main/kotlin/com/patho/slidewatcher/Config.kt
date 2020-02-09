package com.patho.slidewatcher

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(
        prefix = "slidewatcher"
)
class Config {
    lateinit var dirToWatch: List<String>

    lateinit var errorAddresses : List<String>

    var useAuthentication : Boolean = false

    lateinit var authenticationToken : String

}