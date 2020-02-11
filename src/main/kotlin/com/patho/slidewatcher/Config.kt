package com.patho.slidewatcher

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(
        prefix = "slidewatcher"
)
class Config {
    /**
     * dir for slide storage
     */
    lateinit var targetDir: String

    /**
     * dir to watch for new files
     */
    lateinit var dirToWatch: String

    /**
     * emai addresses for errors
     */
    lateinit var errorAddresses: List<String>

    /**
     * use authentication
     */
    var useAuthentication: Boolean = false

    /**
     * authentication token
     */
    lateinit var authenticationToken: String

    /**
     * endpoint for posting new scanned slides
     */
    lateinit var scannedSlideRestEndpoint: String

    /**
     * endpoint for getting infos (name) of slides
     */
    lateinit var slideInfoRestEndpoint: String

    /**
     * endpoint for removing scanned slides
     */
    lateinit var removeScannedSlideRestEndpoint: String

    /**
     * threshold of year that are considered not 19xx
     */
    var oldMillennialYearThreshold: Int = 60

    /**
     * Scan interval for dirToWatch in ms (file listener not possible, not local disk)
     */
    var scanInterval: Long = 1000
}