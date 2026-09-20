package com.myvideoeditor.editor

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.VideoView
import com.myvideoeditor.R

class EditorActivity : Activity() {

    companion object {
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_VIDEO_URI = "video_uri"
    }

    private lateinit var videoPreview: VideoView
    private lateinit var playButton: TextView
    private lateinit var videoTime: TextView

    private var videoPrepared = false

    private val timeHandler =
        Handler(Looper.getMainLooper())

    private val timeUpdater =
        object : Runnable {
            override fun run() {

                if (videoPrepared) {
                    updateVideoTime()
                    timeHandler.postDelayed(
                        this,
                        500
                    )
                }
            }
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

        videoPreview =
            findViewById(R.id.videoPreview)

        playButton =
            findViewById(R.id.playButton)

        videoTime =
            findViewById(R.id.videoTime)

        playButton.text = "▶"

        if (!videoUriString.isNullOrEmpty()) {

            val videoUri =
                Uri.parse(videoUriString)

            videoPreview.setVideoURI(videoUri)

            videoPreview.setOnPreparedListener { mediaPlayer ->

                videoPrepared = true

                mediaPlayer.isLooping = false

                videoPreview.seekTo(1)

                updateVideoTime()

                playButton.text = "▶"

                timeHandler.removeCallbacks(
                    timeUpdater
                )

                timeHandler.post(timeUpdater)
            }

            videoPreview.setOnCompletionListener {

                playButton.text = "▶"

                updateVideoTime()
            }

            videoPreview.setOnErrorListener { _, _, _ ->

                videoPrepared = false
                playButton.text = "▶"
                videoTime.text = "Video could not be played"

                true
            }
        }

        playButton.setOnClickListener {

            if (!videoPrepared) {
                return@setOnClickListener
            }

            if (videoPreview.isPlaying) {

                videoPreview.pause()

                playButton.text = "▶"

            } else {

                if (
                    videoPreview.duration > 0 &&
                    videoPreview.currentPosition >=
                    videoPreview.duration
                ) {
                    videoPreview.seekTo(0)
                }

                videoPreview.start()

                playButton.text = "Ⅱ"

                timeHandler.removeCallbacks(
                    timeUpdater
                )

                timeHandler.post(timeUpdater)
            }
        }
    }

    private fun updateVideoTime() {

        if (!videoPrepared) {
            return
        }

        val current =
            videoPreview.currentPosition

        val duration =
            videoPreview.duration

        videoTime.text =
            "${formatTime(current)} / ${formatTime(duration)}"
    }

    private fun formatTime(
        milliseconds: Int
    ): String {

        val totalSeconds =
            milliseconds / 1000

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    override fun onPause() {
        super.onPause()

        if (::videoPreview.isInitialized &&
            videoPreview.isPlaying
        ) {
            videoPreview.pause()
            playButton.text = "▶"
        }
    }

    override fun onDestroy() {
        timeHandler.removeCallbacks(
            timeUpdater
        )

        if (::videoPreview.isInitialized) {
            videoPreview.stopPlayback()
        }

        super.onDestroy()
    }
}
