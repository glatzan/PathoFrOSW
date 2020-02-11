package com.patho.slidewatcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service


/**
 * Mail service
 */
@Service
class MailService @Autowired constructor(
        private val emailSender: JavaMailSender) {

    fun sendMail(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.setSubject(subject)
        message.setText(text)
        emailSender.send(message)
    }

}