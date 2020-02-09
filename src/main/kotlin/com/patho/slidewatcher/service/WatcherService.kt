package com.patho.slidewatcher.service

import com.patho.slidewatcher.Config
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDate


@Service
class WatcherService @Autowired constructor(
        private val resourceLoader: ResourceLoader,
        private val config: Config) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun watchDir(dir: String) {
        val observer = FileAlterationObserver(getResource(dir).file)

        logger.info("Start ACTIVITY, Monitoring $dir")
        observer.addListener(object : FileAlterationListenerAdaptor() {
            override fun onDirectoryCreate(file: File) {
                logger.info("New Folder Created:" + file.name)
            }

            override fun onDirectoryDelete(file: File) {
                logger.info("Folder Deleted:" + file.name)
            }

            override fun onFileCreate(file: File) {
                logger.info("File Created:" + file.name + ": YOUR ACTION")
            }

            override fun onFileDelete(file: File) {
                logger.info("File Deleted:" + file.name + ": NO ACTION")
            }

            override fun onFileChange(file: File) {
                logger.info("File Renames:" + file.name + ": NO ACTION")
            }
        })
        /* Set to monitor changes for 500 ms */
        /* Set to monitor changes for 500 ms */
        val monitor = FileAlterationMonitor(1000, observer)
        try {
            monitor.start()
        } catch (e: Exception) {
            logger.error("UNABLE TO MONITOR SERVER" + e.message)
            e.printStackTrace()
        }
    }


    private fun getFolder(path: String): String {
        val year = LocalDate.now().year.toString()
        return path.replace("{year}", year)
    }

    private fun getResource(path: String): Resource {
        return resourceLoader.getResource(getFolder(path))
    }
}
