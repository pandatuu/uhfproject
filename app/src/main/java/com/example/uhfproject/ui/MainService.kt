package com.example.uhfproject.ui

import retrofit2.http.POST

interface MainService {

    @POST("http://172.24.1.15:8080/post/inbound")
    suspend fun submitRk()

}