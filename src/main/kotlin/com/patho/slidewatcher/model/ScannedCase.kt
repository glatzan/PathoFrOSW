package com.patho.slidewatcher.model

import com.google.gson.Gson

/**
 * Container for caseID and slideInfos
 */
class ScannedCase(var caseID: String) {

    var scannedSlides = mutableListOf<ScannedSlide>()

    fun toJson() : String{
        return Gson().toJson(this)
    }
}