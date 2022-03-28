package work.matse.blockui

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnAttach

class BlockUIService : Service() {
    private var layoutOverlay: ConstraintLayout? = null
    //private var layoutKeyboard: TableLayout? = null
    private var textViewKey: TextView? = null

    private var windowManager: WindowManager? = null
    private var viewOverlay: View? = null

    private var key: String = ""
    private var currentInput: String = ""

    private var sharedPreferences: SharedPreferences? = null

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

    private fun isComplete(): Boolean {
        return currentInput == key
    }

    private fun closeOverlay() {
        windowManager!!.removeView(viewOverlay)
        viewOverlay = null
    }

    private fun toggleVisibility() {
        if (textViewKey!!.visibility == View.VISIBLE) {
            textViewKey!!.visibility = View.INVISIBLE

            val color = sharedPreferences!!.getInt("darkMode", 0)
            layoutOverlay!!.setBackgroundColor(Color.argb(color, 0, 0, 0))
        } else {
            textViewKey!!.visibility = View.VISIBLE
            layoutOverlay!!.background = getDrawable(R.color.black_overlay)
        }
    }

    private fun createView(): View {
        sharedPreferences = baseContext.getSharedPreferences("BlockUI", Context.MODE_PRIVATE)
        val inflater =
                baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        viewOverlay = inflater.inflate(R.layout.overlay, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewOverlay!!.doOnAttach {
                viewOverlay!!.setOnApplyWindowInsetsListener { v, insets ->
                    viewOverlay!!.windowInsetsController!!.hide(WindowInsets.Type.statusBars())
                    return@setOnApplyWindowInsetsListener insets
                }
            }
        }
        //вывод оповещения!
        layoutOverlay = viewOverlay!!.findViewById(R.id.lyOverlay)

        viewOverlay!!.findViewById<Button>(R.id.btnBack).setOnClickListener {
            toggleVisibility()
        }

        return viewOverlay!!
    }

    private fun generateLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            PixelFormat.TRANSLUCENT)

        layoutParams.gravity = Gravity.TOP or Gravity.RIGHT
        layoutParams.x = 0
        layoutParams.y = 0
        return layoutParams
    }
}