package com.patho.slidewatcher.model

/**
 * Container for scanned slides
 */
class ScannedSlide {

    /**
     * unique slide id in task
     */
    var uniqueSlideID: String = ""

    /**
     * Id of the slide in database
     */
    var slideID: String = ""

    /**
     * current name
     */
    var name: String = ""

    /**
     * Path and name of file
     */
    var path: String = ""

}