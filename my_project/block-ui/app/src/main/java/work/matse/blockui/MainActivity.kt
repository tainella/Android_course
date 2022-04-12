package work.matse.blockui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOn).setOnClickListener {
            doOn()
        }
    }

    override fun onResume() {
        super.onResume()

        Handler().postDelayed(Runnable { initService() }, 200)
    }

    private fun initService() {
        if (checkOverlayDisplayPermission()) {

        }
        else {
            requestOverlayDisplayPermission()
        }
        //enabled the permissions before we can use our MediaRecorder
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
    }

    private fun startmyService() {
        val overlayService = Intent(this, BlockUIService::class.java)
        startForegroundService(overlayService)
    }

    private fun stopmyService() {
        val overlayService = Intent(this, BlockUIService::class.java)
        stopService(overlayService)
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
            startmyService()
        }
        else {
            findViewById<Button>(R.id.btnOn).setText("Включить")
            findViewById<Button>(R.id.btnOn).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE))
            stopmyService()
        }
    }

}