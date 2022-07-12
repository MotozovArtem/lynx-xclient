package org.lynx.http.client

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LynxChatClient {

    @POST("/api/file")
    suspend fun sendFile(@Body message: FileRequest): Response<String>

    @POST("/api/message")
    suspend fun sendMessage(@Body message: MessageRequest): Response<String>

    @POST("/api/cert")
    suspend fun sendCertificate(@Body cert: Certificate): Response<Certificate>
}