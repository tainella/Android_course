package server

import android.net.Uri
import java.io.File

class APIService(private val api: API) {
    suspend fun postscreen_getout(image: File, music: File) = api.getList(image, music)
}