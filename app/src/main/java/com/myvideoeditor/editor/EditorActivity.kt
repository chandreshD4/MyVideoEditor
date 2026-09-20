package com.myvideoeditor.editor

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.VideoView
import com.myvideoeditor.R

class EditorActivity : Activity() {

    companion object {
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_VIDEO_URI = "video_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val projectName =
            intent.getStringExtra(EXTRA_PROJECT_NAME)
                ?: "My Project"

        val videoUriString =
            intent.getStringExtra(EXTRA_VIDEO_URI)

        val editorTitle =
            findViewById<TextView>(R.id.editorTitle)

        editorTitle.text = projectName

        val backButton =
            findViewById<TextView>(R.id.editorBack)

        backButton.setOnClickListener {
            finish()
        }

        val videoPreview =
            findViewById<VideoView>(R.id.videoPreview)

        if (!videoUriString.isNullOrEmpty()) {

            val videoUri =
                Uri.parse(videoUriString)

            videoPreview.setVideoURI(videoUri)

            videoPreview.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
            }
        }

        val playButton =
            findViewById<TextView>(R.id.playButton)

        playButton.setOnClickListener {

            if (videoPreview.isPlaying) {

                videoPreview.pause()
                playButton.text = "▶"

            } else {

                videoPreview.start()
                playButton.text = "Ⅱ"
            }
        }
    }
}
