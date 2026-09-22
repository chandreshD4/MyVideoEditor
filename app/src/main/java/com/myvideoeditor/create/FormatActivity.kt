package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.myvideoeditor.R

class FormatActivity : Activity() {

    companion object {
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_format_selection)

        findViewById<TextView>(R.id.formatBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.formatShorts).setOnClickListener {
            chooseFormat("9:16")
        }

        findViewById<TextView>(R.id.formatYouTube).setOnClickListener {
            chooseFormat("16:9")
        }

        findViewById<TextView>(R.id.formatSquare).setOnClickListener {
            chooseFormat("1:1")
        }

        findViewById<TextView>(R.id.formatFacebook).setOnClickListener {
            chooseFormat("4:5")
        }

        findViewById<TextView>(R.id.formatInstagram).setOnClickListener {
            chooseFormat("3:4")
        }

        findViewById<TextView>(R.id.formatCustom).setOnClickListener {
            chooseFormat("CUSTOM")
        }
    }

    private fun chooseFormat(aspectRatio: String) {

        val intent = Intent(
            this,
            MediaSourceActivity::class.java
        ).apply {
            putExtra(
                EXTRA_ASPECT_RATIO,
                aspectRatio
            )
        }

        startActivity(intent)
    }
}
