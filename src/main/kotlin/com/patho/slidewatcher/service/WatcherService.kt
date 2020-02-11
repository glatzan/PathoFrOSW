package com.patho.slidewatcher.service

import com.patho.slidewatcher.Config
import com.patho.slidewatcher.model.ScannedCase
import com.patho.slidewatcher.model.ScannedSlide
import com.patho.slidewatcher.model.SlideInfoResult
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.apache.commons.io.filefilter.HiddenFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException


@Service
class WatcherService @Autowired constructor(
        private val resourceLoader: ResourceLoader,
        private val config: Config,
        private val restService: RestService,
        private val mailService: MailService) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun watchDir(dir: String) {

        val observer = FileAlterationObserver(dir, FileFilterUtils.or(getFileFilter(), getDirFilter()))

        logger.info("Start ACTIVITY, Monitoring $dir")

        observer.addListener(object : FileAlterationListenerAdaptor() {
            override fun onFileCreate(file: File) {
                processFile(file)
            }
        })

        val monitor = FileAlterationMonitor(config.scanInterval, observer)

        try {
            monitor.start()
        } catch (e: Exception) {
            logger.error("UNABLE TO MONITOR SERVER" + e.message)
            e.printStackTrace()
        }
    }

    /**
     * Scan the whole dir on startup
     */
    fun scanDir(dir : String){
        val files = FileUtils.listFiles(File(dir), getFileFilter(), getDirFilter())

        for(file in files){
            processFile(file)
        }
    }

    private fun processFile(file: File) {
        val name = file.name

        // if matches taskID_unqieSlideID_staining.ndpi xxxxxx_xxx_x*.ndpi
        if (name.matches(Regex("[0-9]{9,} - (.*?)\\.ndpi"))) {
            // file was renamed
            val case = ScannedCase(name.substring(0, 5))
            val slide = ScannedSlide()
            slide.uniqueSlideID = name.substring(7, 9)
            case.scannedSlides.add(slide)

            val infoRequest = restService.getSlideInfo(case.caseID, slide.uniqueSlideID, config.slideInfoRestEndpoint, config.useAuthentication, config.authenticationToken)

            if (infoRequest == null) {
                mailService.sendMail(config.errorAddresses.first(), "Error, could not get SlideInfo",
                        "Error File ${case.toJson()}, orig: ${file.absolutePath}")
                // TODO move file
                return
            }

            val newName = getNewName(name, infoRequest)
            val newFolder = getNewDir(name)

            // setting new name
            slide.name = newFolder + newName

            val result = restService.postScannedSlide(case, config.scannedSlideRestEndpoint, config.useAuthentication, config.authenticationToken)

            // error on post
            if (!result) {
                mailService.sendMail(config.errorAddresses.first(), "Error, could not post scanned slide",
                        "Error File ${case.toJson()}, orig: ${file.absolutePath}")
                return
            }
            try {
                FileUtils.moveFileToDirectory(file, File(newFolder), true)
            } catch (e: IOException) {
                mailService.sendMail(config.errorAddresses.first(), "Error, could move slide",
                        "Error File ${case.toJson()}, orig: ${file.absolutePath}")
                restService.removeScannedSlide(case.caseID, slide.uniqueSlideID, config.scannedSlideRestEndpoint, config.useAuthentication, config.authenticationToken)
                return
            }
        } else {
            logger.debug("Error: file not recognized")
            mailService.sendMail(config.errorAddresses.first(), "Error, filename could not be parsed",
                    "Error File ${file.absolutePath}")
        }
    }

    private fun getFileFilter(): IOFileFilter {
        return FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".ndpi"))
    }

    private fun getDirFilter(): IOFileFilter {
        return FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE, RegexFileFilter("[0-9]{4,}"))
    }

    private fun getNewName(origName: String, dbName: SlideInfoResult): String {
        val caseNr = origName.substring(0, 5)
        val uniqueSlideID = origName.substring(6, 8)

        return "${caseNr}_${uniqueSlideID}_${dbName.name}.ndpi"
    }

    private fun getNewDir(origName: String): String {
        val year = origName.substring(0, 1).toInt()
        // checking if slide is 19xx or 20xx
        val yearString = if (year > config.oldMillennialYearThreshold) {
            "19$year"
        } else {
            "20$year"
        }

        val caseNr = origName.substring(0, 5)

        return "${addSlash(config.targetDir)}${addSlash(yearString)}${addSlash(caseNr)}"
    }

    companion object {
        fun addSlash(str: String): String {
            return if (str.endsWith("/"))
                str
            else
                "$str/"
        }
    }


}
