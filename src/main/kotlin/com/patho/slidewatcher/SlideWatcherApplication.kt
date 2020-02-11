package com.patho.slidewatcher

import com.patho.slidewatcher.service.WatcherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class SlideWatcherApplication @Autowired constructor(
        private val watcher: WatcherService,
        private val config: Config) : CommandLineRunner {

    override fun run(vararg args: String?) {
        watcher.scanDir(config.dirToWatch)
        watcher.watchDir(config.dirToWatch)
    }

}

fun main(args: Array<String>) {
    runApplication<SlideWatcherApplication>(*args)
}
