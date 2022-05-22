package work.matse.blockui

import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import work.matse.blockui.screenshot.CaptureService
import work.matse.blockui.server.API
import work.matse.blockui.server.APIService
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class BlockUIService : Service(), CoroutineScope {
    private var windowManager: WindowManager? = null
    private var viewOverlay: View? = null
    private var sharedPreferences: SharedPreferences? = null
    var timer = Timer()
    var last_mood : String? = null
    var mood : String? = null

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

    //сервис API
    private val service = APIService(retrofit.create(API::class.java))
    //запуск многопоточности
    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Main + job

    //для записи звука
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (viewOverlay == null) {
            windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            windowManager!!.addView(createView(), generateLayoutParams())
            //звук
            output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
            if (!File(output).exists()) {
                File(output).createNewFile()
            }
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(output)
            mediaRecorder?.setMaxDuration(3000)
        }
        return START_NOT_STICKY
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
        val screenService : Intent? = Intent(baseContext, CaptureService::class.java)
        startService(screenService!!)
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val monitor = object : TimerTask() {
            override fun run() {
                val file = File(directory, "inaut.jpg")
                val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
                val body1 = MultipartBody.Part.createFormData("image", file?.name, requestFile)
                launchUI {
                    withIO {
                        startRecording()
                        delay(2000L)
                        stopRecording()
                        println("%%%%%%%%TRYING TO LOAD MUSIC%%%%%%%%")
                        val music = File(Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3")
                        val requestFile2 = RequestBody.create(MediaType.parse("audio/mp3"), music)
                        val body2 = MultipartBody.Part.createFormData("music", music?.name, requestFile2)
                        mood = service.postscreen_getout(body1, body2)
                    } //withIO помогает получать данные из другого потока, так быстрее
                }
                //file.delete()
                println(mood)

                if (last_mood != mood) {
                    //выбор текста на оповещении
                    last_mood = mood
                    when (mood) {
                        "anger" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "anger")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        }//alert.setText("@string/anger")
                        "calm" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "calm")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        } //alert.setText("@string/calm")
                        "happy" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "happy")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        } //alert.setText("@string/happy")
                        "sad" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "sad")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        } //alert.setText("@string/sad")
                        "disgust" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "disgust")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        } //alert.setText("@string/disgust")
                        "sarcasm" -> {
                            val overlayService = Intent(getBaseContext(), ToastService::class.java).apply {
                                putExtra(EXTRA_MESSAGE, "sarcasm")
                            }
                            ContextCompat.startForegroundService(getBaseContext(), overlayService)
                        } //alert.setText("@string/sarcasm")
                    }
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

    /*private fun loadImageFromStorage(path: String) {
        try {
            val f = File(path, "profile.jpg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            val img: ImageView = findViewById(R.id.imgPicker) as ImageView
            img.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
    */

    private fun getScreenShotFromView(v: View): Bitmap? {
        var screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        v.draw(canvas)

        return screenshot
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        if(state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }
    }

}