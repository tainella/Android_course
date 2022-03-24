package com.example.third_activity.server

class APIService(private val api: API) {

    suspend fun getList() = api.getList()

}