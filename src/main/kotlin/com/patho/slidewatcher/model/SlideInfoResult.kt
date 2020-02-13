package com.patho.slidewatcher.model

import java.io.Serializable

/**
 * Result for slide info request (db name of slide = slideID)
 */
class SlideInfoResult constructor(var name: String = "", var uniqueID: String, var slideID: String) : Serializable