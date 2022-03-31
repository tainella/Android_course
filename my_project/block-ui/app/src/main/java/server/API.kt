package server

import android.net.Uri
import retrofit2.http.Body
import retrofit2.http.POST

interface API {
    @POST("images/process")
    suspend fun getList(@Body image: Uri) : List<String> //suspend ассинхронные
}