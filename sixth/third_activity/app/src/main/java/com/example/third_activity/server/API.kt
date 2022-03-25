package com.example.third_activity.server

import retrofit2.http.GET

interface API {
    @GET("api/hello/list")
    suspend fun getList() : List<String> //suspend ассинхронные
}