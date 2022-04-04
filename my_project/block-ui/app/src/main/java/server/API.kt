package server

import retrofit2.http.Body
import retrofit2.http.POST
import java.io.File

interface API {
    @POST("images/process")
    suspend fun getList(@Body file: File) : List<String> //suspend ассинхронные
}