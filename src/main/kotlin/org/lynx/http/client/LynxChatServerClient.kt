package org.lynx.http.client

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LynxChatServerClient {
    @POST("/token/generate-token")
    suspend fun authentication(@Body credentials: AuthCredentialsRequest): Response<AuthTokenResponse>

    @GET("/user/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserResponse>

    @GET("/user")
    suspend fun getUsers(): Response<List<UserResponse>>
}