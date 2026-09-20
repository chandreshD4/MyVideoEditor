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

    private lateinit var zoomOutButton: TextView
    private lateinit var zoomInButton: TextView
    private lateinit var zoomValue: TextView

    private var videoPrepared = false

    private var currentZoom =
        TimelineMetrics.DEFAULT_ZOOM

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
                        100
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

        editorTitle.text =
            projectName

        val backButton =
            findViewById<TextView>(
                R.id.editorBack
            )

        backButton.setOnClickListener {
            finish()
        }

        val saveButton =
            findViewById<TextView>(
                R.id.editorSave
            )

        saveButton.setOnClickListener {
            saveEditorState()
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

        zoomOutButton =
            findViewById(
                R.id.zoomOutButton
            )

        zoomInButton =
            findViewById(
                R.id.zoomInButton
            )

        zoomValue =
            findViewById(
                R.id.zoomValue
            )

        playButton.text =
            "▶"

        setupZoomControls()
        setupTimeline()
        setupVideo(videoUriString)
    }
    private fun setupZoomControls() {

        updateZoomText()

        zoomInButton.setOnClickListener {

            currentZoom =
                TimelineMetrics.zoomIn(
                    currentZoom
                )

            timelineView.setZoom(
                currentZoom
            )

            updateZoomText()
        }

        zoomOutButton.setOnClickListener {

            currentZoom =
                TimelineMetrics.zoomOut(
                    currentZoom
                )

            timelineView.setZoom(
                currentZoom
            )

            updateZoomText()
        }
    }

    private fun updateZoomText() {

        zoomValue.text =
            TimelineMetrics.formatZoom(
                currentZoom
            )
    }

    private fun setupTimeline() {

        timelineView.setZoom(
            currentZoom
        )

        timelineView.setOnPositionChangedListener {
            position ->

            if (!videoPrepared) {
                return@setOnPositionChangedListener
            }

            videoPreview.seekTo(
                position
            )

            videoTime.text =
                "${TimelineMetrics.formatTime(position.toLong())} / " +
                TimelineMetrics.formatTime(
                    videoPreview.duration.toLong()
                )

            if (videoPreview.isPlaying) {

                videoPreview.pause()

                playButton.text =
                    "▶"

                timeHandler.removeCallbacks(
                    timeUpdater
                )
            }
        }
    }

    private fun setupVideo(
        videoUriString: String?
    ) {

        if (videoUriString.isNullOrEmpty()) {
            videoTime.text =
                "00:00 / 00:00"

            return
        }

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
                "${TimelineMetrics.formatTime(0L)} / " +
                TimelineMetrics.formatTime(
                    duration.toLong()
                )

            playButton.text =
                "▶"

            videoPreview.seekTo(
                0
            )
        }

        videoPreview.setOnCompletionListener {

            playButton.text =
                "▶"

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

            playButton.text =
                "▶"

            videoTime.text =
                "Video could not be played"

            timeHandler.removeCallbacks(
                timeUpdater
            )

            true
        }

        playButton.setOnClickListener {

            if (!videoPrepared) {
                return@setOnClickListener
            }

            if (
                videoPreview.isPlaying
            ) {

                videoPreview.pause()

                playButton.text =
                    "▶"

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

                    videoPreview.seekTo(
                        0
                    )

                    timelineView.setPosition(
                        0
                    )
                }

                videoPreview.start()

                playButton.text =
                    "Ⅱ"

                timeHandler.removeCallbacks(
                    timeUpdater
                )

                timeHandler.post(
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
            "${TimelineMetrics.formatTime(position.toLong())} / " +
            TimelineMetrics.formatTime(
                duration.toLong()
            )

        timelineView.setPosition(
            position
        )
    }

    private fun saveEditorState() {

        if (!videoPrepared) {
            return
        }

        timelineView.setPosition(
            videoPreview.currentPosition
        )

        videoTime.text =
            "${TimelineMetrics.formatTime(
                videoPreview.currentPosition.toLong()
            )} / " +
            TimelineMetrics.formatTime(
                videoPreview.duration.toLong()
            )
    }

    override fun onPause() {

        super.onPause()

        if (
            ::videoPreview.isInitialized &&
            videoPreview.isPlaying
        ) {

            videoPreview.pause()

            playButton.text =
                "▶"

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

        if (
            ::timelineView.isInitialized
        ) {

            timelineView.clearThumbnails()
        }

        super.onDestroy()
    }
}
