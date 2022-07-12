package org.lynx.http.client

import com.google.gson.annotations.SerializedName

data class CertRequest(
    @SerializedName("username") val username: String,
    @SerializedName("pubKey") val pubKey: String
)

data class MessageRequest(
    @SerializedName("username") val username: String, // sender
    @SerializedName("message") val message: String,
    @SerializedName("iv") val iv: String,
    @SerializedName("chat") val chat: String
)

data class FileRequest(
    @SerializedName("username") val username: String, // sender
    @SerializedName("fileData") val fileData: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("iv") val iv: String,
    @SerializedName("chat") val chat: String
)