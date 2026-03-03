package com.cello

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class HttpService {

    private val client = OkHttpClient()

    fun uploadImage(imageBytes: ByteArray, callback: Callback) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes))
            .build()

        val request = Request.Builder()
            .url("http://172.20.10.2:8088/detect")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }
}
