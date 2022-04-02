package work.matse.blockui

import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import androidx.core.view.doOnAttach
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import server.API
import server.APIService
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class BlockUIService : Service(), CoroutineScope {
    private var windowManager: WindowManager? = null
    private var viewOverlay: View? = null
    private var sharedPreferences: SharedPreferences? = null
    var timer = Timer()
    var list : List<String>? = null

    //выполняет запросы к серверу
    private val okhttp = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    //конвертация джейсона из сервера
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okhttp)
        .baseUrl("http://70.34.216.175:8000/")
        .build()

    //сервис
    private val service = APIService(retrofit.create(API::class.java))
    //запуск многопоточности
    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Main + job

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (viewOverlay == null) {
            windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            windowManager!!.addView(createView(), generateLayoutParams())
        }
        return START_NOT_STICKY
    }

    private fun toggleVisibility() {
        if (viewOverlay!!.visibility == View.VISIBLE) {
            viewOverlay!!.visibility = View.INVISIBLE
            timer.cancel()
            timer.purge()
        }
        else {
            viewOverlay!!.visibility = View.VISIBLE
        }
    }

    //упрощение работы с многопоточностью
    fun CoroutineScope.launchUI(callback: suspend () -> Unit) = launch(Dispatchers.Main) { callback() }
    suspend fun <T> withIO(callback: suspend () -> T) = withContext(Dispatchers.Main) { callback() }

    private fun createView(): View {
        sharedPreferences = baseContext.getSharedPreferences("BlockUI", Context.MODE_PRIVATE)
        val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewOverlay = inflater.inflate(R.layout.overlay, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewOverlay!!.doOnAttach {
                viewOverlay!!.setOnApplyWindowInsetsListener { v, insets ->
                    viewOverlay!!.windowInsetsController!!.hide(WindowInsets.Type.statusBars())
                    return@setOnApplyWindowInsetsListener insets
                }
            }
        }
        viewOverlay!!.findViewById<Button>(R.id.btnBack).setOnClickListener {
            toggleVisibility()
        }

        val monitor = object : TimerTask() {
            override fun run() {
                val screen = getScreenShotFromView(viewOverlay!!)
                launchUI {
                        withIO {
                            list = service.postscreen_getout(saveMediaToStorage(screen!!)!!) } //withIO помогает получать данные из другого потока, так быстрее
                    println(list)
                }
            }
        }
        timer.schedule(monitor, 1000, 1000)

        return viewOverlay!!
    }

    private fun generateLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_FULLSCREEN and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT)

        layoutParams.gravity = Gravity.TOP or Gravity.RIGHT
        layoutParams.x = 0
        layoutParams.y = 0
        return layoutParams
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        var screenshot: Bitmap? = null
        screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        v.draw(canvas)
        return screenshot
    }

    private fun saveMediaToStorage(bitmap: Bitmap) : Uri? {
        val filename = "${System.currentTimeMillis()}.jpg"

        this.contentResolver?.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            return imageUri
        }
        return null
    }

}