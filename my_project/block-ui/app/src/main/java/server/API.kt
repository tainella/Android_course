package server

import retrofit2.http.*
import java.io.File

interface API {
    @Multipart
    @POST("images/process")
    suspend fun getList(@Part("screen") file: File, @Part("music") music: File) : List<String> //suspend ассинхронные
}