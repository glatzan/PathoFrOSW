package com.patho.slidewatcher.service

import com.google.gson.Gson
import com.patho.slidewatcher.model.ScannedCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.lang.Exception

@Service
class RestService {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun uploadFile(scannedCase: ScannedCase, targetURl: String, useAuthentication: Boolean = false, token: String = ""): Boolean {
        val bodyMap: MultiValueMap<String, Any> = LinkedMultiValueMap()
        bodyMap.add("caseID", scannedCase.caseID)
        bodyMap.add("slides", Gson().toJson(scannedCase.scannedSlides))

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        if (useAuthentication)
            headers.set("Authorization", token);

        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(bodyMap, headers)

        logger.debug("Sending pdf (${scannedCase.caseID}) to $targetURl")

        return try {
            val restTemplate = RestTemplate()
            val response: ResponseEntity<String> = restTemplate.exchange(targetURl,
                    HttpMethod.POST, requestEntity, String::class.java)
            logger.debug("response status: " + response.statusCode)
            logger.debug("response body: " + response.body)
            response.statusCode.toString() == "200 OK"
        } catch (e: Exception) {
            logger.debug("Failed!")
            false
        }
    }

    fun getSlideInfo(taskID: String, uniqueSlideID: String): String {
        return ""
    }
}