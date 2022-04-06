package server

import okhttp3.MultipartBody
import retrofit2.http.*
import java.io.File

interface API {
    @Multipart
    @POST("images/process")
    suspend fun getList(@Part screen: MultipartBody.Part, @Part music: MultipartBody.Part) : String //suspend ассинхронные
}