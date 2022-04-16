package work.matse.blockui.server

import android.net.Uri
import okhttp3.MultipartBody
import java.io.File

class APIService(private val api: API) {
    suspend fun postscreen_getout(image: MultipartBody.Part, music: MultipartBody.Part) = api.getList(image, music)
}