package work.matse.blockui.screenshot

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler


class CaptureActivity : Activity() {

    companion object {
        private const val REQUEST_CAPTURE = 1//13

        var projection: MediaProjection? = null
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("@@@@@@@CAPTURE ACTIVITY STARTED@@@@@@")
        Handler().postDelayed({
                mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
        }, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Handler().postDelayed({
                    projection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
                    val intent = Intent(this, CaptureService::class.java)
                        .setAction(CaptureService.ACTION_ENABLE_CAPTURE)
                    startService(intent)
                }, 100)
            } else {
                projection = null
            }
        }
        finish()
    }
}