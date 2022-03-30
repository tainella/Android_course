package work.matse.blockui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences: SharedPreferences? = getSharedPreferences("BlockUI", Context.MODE_PRIVATE)
        findViewById<Button>(R.id.btnOn).setOnClickListener {
            doOn()
        }
    }

    override fun onResume() {
        super.onResume()

        Handler().postDelayed(Runnable {
            initService()
        }, 200)
    }

    private fun initService() {
        if (checkOverlayDisplayPermission()) {
            //startService()
        }
        else {
            requestOverlayDisplayPermission()
        }
    }

    private fun startService() {
        val overlayService = Intent(this, BlockUIService::class.java)
        ContextCompat.startForegroundService(this, overlayService)
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayDisplayPermission() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.permissionTitle)
        builder.setMessage(R.string.permissionDescription)
        builder.setPositiveButton(R.string.permissionGoToSettings, DialogInterface.OnClickListener { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, Activity.RESULT_OK)
        })

        val dialog = builder.create()
        dialog.show()
    }

    fun doOn() {
        if (findViewById<Button>(R.id.btnOn).text == "Включить") {
            findViewById<Button>(R.id.btnOn).setText("Выключить")
            findViewById<Button>(R.id.btnOn).setBackgroundTintList(ColorStateList.valueOf(Color.BLACK))
        }
        else {
            findViewById<Button>(R.id.btnOn).setText("Включить")
            findViewById<Button>(R.id.btnOn).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE))
        }
    }

    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {
            val mPath: String = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"

            val v1: View = window.decorView.rootView
            v1.setDrawingCacheEnabled(true)
            val bitmap: Bitmap = Bitmap.createBitmap(v1.getDrawingCache())
            v1.setDrawingCacheEnabled(false)
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            //в этом варианте открыть
            openScreenshot(imageFile)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun openScreenshot(imageFile: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri = Uri.fromFile(imageFile)
        intent.setDataAndType(uri, "image/*")
        startActivity(intent)
    }
}