package com.myvideoeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    private val PICK_VIDEO = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val createButton = findViewById<TextView>(R.id.createNewVideo)

        createButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }

            startActivityForResult(intent, PICK_VIDEO)
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK) {
            val videoUri = data?.data

            if (videoUri != null) {
                // अगला चरण: वीडियो को Editor Screen में दिखाएँगे
            }
        }
    }
}
