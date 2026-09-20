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
    private lateinit var timelineView: TimelineView

    private var videoPrepared = false

    private val timeHandler =
        Handler(Looper.getMainLooper())

    private val timeUpdater =
        object : Runnable {

            override fun run() {

                if (
                    videoPrepared &&
                    ::videoPreview.isInitialized &&
                    videoPreview.isPlaying
                ) {

                    updateEditorPosition()

                    timeHandler.postDelayed(
                        this,
                        200
                    )
                }
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_editor
        )

        val projectName =
            intent.getStringExtra(
                EXTRA_PROJECT_NAME
            ) ?: "My Project"

        val videoUriString =
            intent.getStringExtra(
                EXTRA_VIDEO_URI
            )

        val editorTitle =
            findViewById<TextView>(
                R.id.editorTitle
            )

        editorTitle.text = projectName

        val backButton =
            findViewById<TextView>(
                R.id.editorBack
            )

        backButton.setOnClickListener {
            finish()
        }

        videoPreview =
            findViewById(
                R.id.videoPreview
            )

        playButton =
            findViewById(
                R.id.playButton
            )

        videoTime =
            findViewById(
                R.id.videoTime
            )

        timelineView =
            findViewById(
                R.id.timelineView
            )

        playButton.text = "▶"

        if (!videoUriString.isNullOrEmpty()) {

            val videoUri =
                Uri.parse(
                    videoUriString
                )

            videoPreview.setVideoURI(
                videoUri
            )

            timelineView.setVideoUri(
                videoUri
            )

            videoPreview.setOnPreparedListener {

                videoPrepared = true

                val duration =
                    videoPreview.duration

                timelineView.setDuration(
                    duration
                )

                timelineView.setPosition(
                    0
                )

                videoTime.text =
                    "00:00 / ${
                        formatTime(duration)
                    }"

                playButton.text = "▶"

                videoPreview.start()

                videoPreview.postDelayed({

                    if (
                        videoPrepared &&
                        videoPreview.isPlaying
                    ) {

                        videoPreview.pause()

                        videoPreview.seekTo(
                            0
                        )

                        playButton.text = "▶"

                        updateEditorPosition()
                    }

                }, 100)
            }

            videoPreview.setOnCompletionListener {

                playButton.text = "▶"

                timelineView.setPosition(
                    videoPreview.duration
                )

                updateEditorPosition()

                timeHandler.removeCallbacks(
                    timeUpdater
                )
            }

            videoPreview.setOnErrorListener {
                    _, _, _ ->

                videoPrepared = false

                playButton.text = "▶"

                videoTime.text =
                    "Video could not be played"

                timeHandler.removeCallbacks(
                    timeUpdater
                )

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

                timeHandler.removeCallbacks(
                    timeUpdater
                )

                updateEditorPosition()

            } else {

                if (
                    videoPreview.duration > 0 &&
                    videoPreview.currentPosition >=
                    videoPreview.duration
                ) {

                    videoPreview.seekTo(0)

                    timelineView.setPosition(
                        0
                    )
                }

                videoPreview.start()

                playButton.text = "Ⅱ"

                timeHandler.removeCallbacks(
                    timeUpdater
                )

                timeHandler.post(
                    timeUpdater
                )
            }
        }

        timelineView.setOnPositionChangedListener {
                position ->

            if (!videoPrepared) {
                return@setOnPositionChangedListener
            }

            videoPreview.seekTo(
                position
            )

            videoTime.text =
                "${formatTime(position)} / ${
                    formatTime(
                        videoPreview.duration
                    )
                }"

            if (videoPreview.isPlaying) {

                videoPreview.pause()

                playButton.text = "▶"

                timeHandler.removeCallbacks(
                    timeUpdater
                )
            }
        }
    }

    private fun updateEditorPosition() {

        if (!videoPrepared) {
            return
        }

        val position =
            videoPreview.currentPosition

        val duration =
            videoPreview.duration

        videoTime.text =
            "${formatTime(position)} / ${
                formatTime(duration)
            }"

        timelineView.setPosition(
            position
        )
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

        if (
            ::videoPreview.isInitialized &&
            videoPreview.isPlaying
        ) {

            videoPreview.pause()

            playButton.text = "▶"

            timeHandler.removeCallbacks(
                timeUpdater
            )

            updateEditorPosition()
        }
    }

    override fun onDestroy() {

        timeHandler.removeCallbacks(
            timeUpdater
        )

        if (
            ::videoPreview.isInitialized
        ) {

            videoPreview.stopPlayback()
        }

        super.onDestroy()
    }
}
