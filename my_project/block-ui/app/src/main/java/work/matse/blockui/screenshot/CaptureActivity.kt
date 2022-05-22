package work.matse.blockui.screenshot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import work.matse.blockui.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class CaptureActivity : Activity() {

    companion object {
        var image: Image? = null
        private const val REQUEST_CAPTURE = 1//13
        var projection: MediaProjection? = null
    }

    val filename = "inaut.jpg"
    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    var fos: OutputStream? = null
    var timer = Timer()
    var screenWindowWidth: Int? = null
    var screenWindowHeight: Int? = null
    var display: VirtualDisplay? = null
    private val capture = Capture(this)
    val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        screenWindowWidth = baseContext.resources.displayMetrics.widthPixels
        screenWindowHeight = baseContext.resources.displayMetrics.heightPixels

        Handler().postDelayed({
                mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
        }, 1000)
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Handler().postDelayed({
                    projection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
                    projection.run {
                        this?.let {
                            capture.run(it) {
                                val monitor = object : TimerTask() {
                                    override fun run() {
                                        saveToInternalStorage(capture.main_bitmap!!)
                                        println("TRYING TO SAVE")
                                    }
                                }
                                timer.schedule(monitor, 1000, 1000)
                            }
                        }
                    }
                }, 100)
            } else {
                projection = null
            }
        }
        finish()
    }

    private fun saveMediaToStorage(image: Bitmap) : String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (File(imagesDir, filename).exists()) {
                File(imagesDir, filename).delete()
                mainHandler.postDelayed({}, 1000)
                callBroadCast()
                if(File(imagesDir, filename).exists()) {
                    println("ERROR IN DELETING")
                }
            }
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        }
        else {
            val file = File(imagesDir, filename)
            fos = FileOutputStream(file)
        }
        image.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos?.flush()
        fos?.close()
        var str : String = imagesDir.toString()
        str += "/$filename"
        return str
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "inaut.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    fun callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            MediaScannerConnection.scanFile(this, arrayOf(Environment.getExternalStorageDirectory().toString()), null, null)
        }
        else {
            sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (projection != null) {
            display?.release()
            projection?.run { stop() }
        }
    }
}