package server

import android.net.Uri

class APIService(private val api: API) {
    suspend fun postscreen_getout(image: Uri) = api.getList(image)
}