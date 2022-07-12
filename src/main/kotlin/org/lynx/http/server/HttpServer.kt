package org.lynx.http.server

import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import org.lynx.http.client.Certificate
import org.lynx.http.client.MessageRequest
import org.lynx.service.CryptographyService
import org.lynx.service.MessageService
import org.lynx.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.lynx.http.client.FileRequest

class HttpServer(
    private val port: Int,
    private val messageService: MessageService,
    private val cryptographyService: CryptographyService,
    private val userService: UserService
) : NanoHTTPD(port) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(HttpServer::class.java)
    }

    override fun start() {
        super.start()
        log.info("Http server started on ${port}")
    }

    override fun serve(session: IHTTPSession?): Response {
        log.debug("Received request with URI: ${session?.uri}")
        if (session == null) {
            return newFixedLengthResponse("Session is null. Strange behavior.")
        }
        if (session.method != Method.POST) {
            log.debug(
                "Received non-POST request from ${session.remoteHostName} with IP ${session.remoteIpAddress}"
            )
            return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                "application/json",
                """{"status":"Method not allowed"}"""
            )
        }

        return routeRequests(session)
    }

    private fun routeRequests(session: IHTTPSession): Response {
        try {
            return when (session.uri) {
                "/api/message" -> onMessage(session)
                "/api/file" -> onFile(session)
                "/api/cert" -> onCert(session)
                else -> newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    """{"status":"Route not found"}"""
                )
            }
        } catch (e: Exception) {
            log.error("Exception while processing request ${e.message}", e)
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                """{"status":"Internal error"}"""
            )
        }
    }

    private fun onMessage(session: IHTTPSession): Response {
        val requestBody = mutableMapOf<String, String>()
        session.parseBody(requestBody)
        val receivedMessage = Gson().fromJson(requestBody["postData"], MessageRequest::class.java)
        messageService.receiveMessage(receivedMessage)
        return newFixedLengthResponse("OK")
    }

    private fun onFile(session: IHTTPSession): Response {
        val requestBody = mutableMapOf<String, String>()
        session.parseBody(requestBody)
        val receivedFile = Gson().fromJson(requestBody["postData"], FileRequest::class.java)
        messageService.receiveFile(receivedFile)
        return newFixedLengthResponse("OK")
    }

    private fun onCert(session: IHTTPSession): Response {
        val requestBody = mutableMapOf<String, String>()
        session.parseBody(requestBody)
        val receivedCert = Gson().fromJson(requestBody["postData"], Certificate::class.java)
        cryptographyService.generateSharedKeyForAbonent(
            receivedCert.username,
            receivedCert.publicKey
        )
        val certificate = Certificate(
            userService.getCurrentUser().username,
            cryptographyService.getPublicKeyAsBase64()
        )
        val certificateJson = Gson().toJson(certificate)
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            certificateJson
        )
    }

}