package com.patho.slidewatcher.service

import com.google.gson.Gson
import com.patho.slidewatcher.model.ScannedCase
import com.patho.slidewatcher.model.SlideInfoResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*


@Service
class RestService {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Post data for scanned slides
     */
    fun postScannedSlide(case: ScannedCase, targetURl: String, useAuthentication: Boolean = false, token: String = ""): Boolean {
        val bodyMap: MultiValueMap<String, Any> = LinkedMultiValueMap()
        bodyMap.add("caseID", case.caseID)
        bodyMap.add("slides", Gson().toJson(case.scannedSlides))

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        if (useAuthentication)
            headers.set("Authorization", token);

        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(bodyMap, headers)

        logger.debug("Sending pdf (${case.caseID}) to $targetURl")

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

    /**
     * Remove scanned slide
     */
    fun removeScannedSlide(caseID: String, uniqueSlideID: String, targetURl: String, useAuthentication: Boolean = false, token: String = ""): Boolean {
        val util = getRestTemplateForGetRequest(targetURl, useAuthentication, token, caseID, uniqueSlideID)

        try {
            val result = RestTemplate().exchange(util.second.toUriString(), HttpMethod.GET, util.first, String::class.java)
            if (result.body.isNullOrEmpty() && result.body == "success")
                return true
        } catch (e: RestClientException) {
        }
        return false
    }

    /**
     * Requesting slide infos (db name = slideID)
     */
    fun getSlideInfo(caseID: String, uniqueSlideID: String, targetURl: String, useAuthentication: Boolean = false, token: String = ""): Optional<SlideInfoResult> {
        val util = getRestTemplateForGetRequest(targetURl, useAuthentication, token, caseID, uniqueSlideID)

        return try {
            val result = RestTemplate().exchange(util.second.toUriString(), HttpMethod.GET, util.first, SlideInfoResult::class.java)
            Optional.ofNullable(result.body)
        } catch (e: RestClientException) {
            Optional.empty()
        }
    }

    private fun getRestTemplateForGetRequest(targetURl: String, useAuthentication: Boolean = false, token: String = "", caseID: String, uniqueSlideID: String): Pair<HttpEntity<String>, UriComponentsBuilder> {
        val headers = HttpHeaders()
        if (useAuthentication)
            headers.set("Authorization", token);

        val params: MutableMap<String, String> = HashMap()
        params["caseID"] = caseID
        params["uniqueSlideID"] = uniqueSlideID

        val builder = UriComponentsBuilder.fromHttpUrl(targetURl)
                .queryParam("caseID", caseID)
                .queryParam("uniqueSlideID", uniqueSlideID)

        val entity = HttpEntity("parameters", headers)

        return Pair(entity, builder)
    }
}