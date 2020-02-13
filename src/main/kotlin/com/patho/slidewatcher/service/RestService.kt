package com.patho.slidewatcher.service

import com.google.gson.Gson
import com.patho.slidewatcher.Config
import com.patho.slidewatcher.model.ScannedCase
import com.patho.slidewatcher.model.SlideInfoResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class RestService @Autowired constructor(
        private val config: Config) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Post data for scanned slides
     */
    fun postScannedSlide(case: ScannedCase): Boolean {
        val bodyMap: MultiValueMap<String, Any> = LinkedMultiValueMap()
        bodyMap.add("caseID", case.caseID)
        bodyMap.add("slides", Gson().toJson(case.scannedSlides))

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        if (config.useAuthentication)
            headers.set("Authorization", config.authenticationToken);

        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(bodyMap, headers)

        logger.debug("Sending pdf (${case.caseID}) to ${config.scannedSlideRestEndpoint}")

        return try {
            val restTemplate = RestTemplate()
            val response: ResponseEntity<String> = restTemplate.exchange(config.scannedSlideRestEndpoint,
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
    fun removeScannedSlide(caseID: String, slideName: String): Boolean {
        return try {
            val uri = config.removeScannedSlideRestEndpoint.replace("\$caseID", caseID).replace("\$slideName", slideName)
            val result: ResponseEntity<String> = RestTemplate().exchange<String>(uri, HttpMethod.GET, getHeaderForGetRequest(), String::class.java)
            !result.body.isNullOrEmpty() && result.body?.startsWith("Success:") == true
        } catch (e: RestClientException) {
            false
        }
    }

    /**
     * Requesting slide infos (db name = slideID)
     */
    fun getSlideInfo(caseID: String, uniqueSlideID: String): Optional<SlideInfoResult> {
        return try {
            val uri = config.slideInfoRestEndpoint.replace("\$caseID", caseID).replace("\$uniqueSlideID", uniqueSlideID)
            val result: ResponseEntity<SlideInfoResult> = RestTemplate().exchange<SlideInfoResult>(uri, HttpMethod.GET, getHeaderForGetRequest(), SlideInfoResult::class.java)
            Optional.ofNullable(result.body)
        } catch (e: RestClientException) {
            Optional.empty()
        }
    }

    private fun getHeaderForGetRequest(): HttpEntity<String> {
        val headers = HttpHeaders()
        if (config.useAuthentication)
            headers.set("Authorization", config.authenticationToken);
        return HttpEntity("parameters", headers)
    }
}