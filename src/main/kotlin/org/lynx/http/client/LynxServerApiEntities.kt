package org.lynx.http.client

data class UserResponse(
    val name: String,
    val domain: List<String>,
    val online: Boolean
)

data class AuthTokenResponse(
    val token: String,
    val username: String
)

data class AuthCredentialsRequest(
    val username: String,
    val password: String
)

data class Certificate(
    val username: String,
    val publicKey: String
)