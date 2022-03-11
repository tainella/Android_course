package com.example.third_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.third_activity.R
import kotlinx.android.synthetic.main.activity_card.*

class CardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        val count = intent.getIntExtra("count", 0)

        tvItem.text = count.toString()
        tvDescription.text = "Карточка №$count"

    }
}