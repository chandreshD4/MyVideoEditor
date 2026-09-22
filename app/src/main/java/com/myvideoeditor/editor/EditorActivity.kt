package com.myvideoeditor.editor

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView

class EditorActivity : Activity() {

    companion object {
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_VIDEO_URI = "video_uri"

        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_SOURCE_TYPE = "source_type"
        const val EXTRA_BACKGROUND_COLOR = "background_color"
    }

    private lateinit var videoView: VideoView
    private lateinit var playButton: TextView
    private lateinit var timelineTrack: LinearLayout

    private var sourceType = "blank"
    private var aspectRatio = "9:16"
    private var mediaUri: String? = null
    private var backgroundColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        aspectRatio =
            intent.getStringExtra(EXTRA_ASPECT_RATIO)
                ?: "9:16"

        sourceType =
            intent.getStringExtra(EXTRA_SOURCE_TYPE)
                ?: "blank"

        mediaUri =
            intent.getStringExtra(EXTRA_VIDEO_URI)

        backgroundColor =
            intent.getIntExtra(
                EXTRA_BACKGROUND_COLOR,
                Color.BLACK
            )

        buildEditor()
    }

    private fun buildEditor() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 9, 15))
        }

        root.addView(
            createTopBar(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52.dp()
            )
        )

        val workspace = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        workspace.addView(
            createLeftTools(),
            LinearLayout.LayoutParams(
                72.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(12, 14, 21))
        }

        center.addView(
            createPreview(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        center.addView(
            createPreviewControls(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48.dp()
            )
        )

        workspace.addView(
            center,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        workspace.addView(
            createRightTools(),
            LinearLayout.LayoutParams(
                72.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            workspace,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            createTimeline(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                180.dp()
            )
        )

        setContentView(root)
    }

    private fun createTopBar(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.rgb(15, 18, 27))
            setPadding(
                8.dp(),
                0,
                8.dp(),
                0
            )
        }

        val back = toolButton("‹", 28)

        back.setOnClickListener {
            finish()
        }

        bar.addView(
            back,
            LinearLayout.LayoutParams(
                48.dp(),
                48.dp()
            )
        )

        val title = TextView(this).apply {
            text = "New Project"
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
        }

        bar.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        val ratio = TextView(this).apply {
            text = aspectRatio
            setTextColor(Color.LTGRAY)
            textSize = 12f
            gravity = Gravity.CENTER
        }

        bar.addView(
            ratio,
            LinearLayout.LayoutParams(
                70.dp(),
                48.dp()
            )
        )

        val undo = toolButton("↶", 20)
        val redo = toolButton("↷", 20)

        bar.addView(
            undo,
            LinearLayout.LayoutParams(
                45.dp(),
                48.dp()
            )
        )

        bar.addView(
            redo,
            LinearLayout.LayoutParams(
                45.dp(),
                48.dp()
            )
        )

        val export = TextView(this).apply {
            text = "EXPORT"
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            setBackgroundColor(
                Color.rgb(76, 82, 210)
            )
        }

        bar.addView(
            export,
            LinearLayout.LayoutParams(
                82.dp(),
                38.dp()
            )
        )

        return bar
    }

    private fun createPreview(): View {

        val frame = FrameLayout(this).apply {
            setBackgroundColor(
                Color.rgb(4, 5, 8)
            )
        }

        if (sourceType == "video" &&
            !mediaUri.isNullOrEmpty()
        ) {

            videoView = VideoView(this)

            videoView.setVideoURI(
                Uri.parse(mediaUri)
            )

            frame.addView(
                videoView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            )

        } else if (
            sourceType == "image" &&
            !mediaUri.isNullOrEmpty()
        ) {

            val image =
                android.widget.ImageView(this).apply {

                    setImageURI(
                        Uri.parse(mediaUri)
                    )

                    scaleType =
                        android.widget.ImageView.ScaleType
                            .FIT_CENTER
                }

            frame.addView(
                image,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

        } else {

            frame.setBackgroundColor(
                backgroundColor
            )

            val emptyText = TextView(this).apply {
                text = "No media selected"
                setTextColor(
                    Color.argb(
                        150,
                        255,
                        255,
                        255
                    )
                )
                textSize = 14f
                gravity = Gravity.CENTER
            }

            frame.addView(
                emptyText,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        return frame
    }

    private fun createPreviewControls(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(
                Color.rgb(10, 12, 18)
            )
        }

        playButton = toolButton("▶", 20)

        playButton.setOnClickListener {

            if (::videoView.isInitialized) {

                if (videoView.isPlaying) {
                    videoView.pause()
                    playButton.text = "▶"
                } else {
                    videoView.start()
                    playButton.text = "Ⅱ"
                }
            }
        }

        bar.addView(
            playButton,
            LinearLayout.LayoutParams(
                55.dp(),
                45.dp()
            )
        )

        val time = TextView(this).apply {
            text = "00:00 / 00:00"
            setTextColor(Color.LTGRAY)
            textSize = 11f
            gravity = Gravity.CENTER
        }

        bar.addView(
            time,
            LinearLayout.LayoutParams(
                120.dp(),
                45.dp()
            )
        )

        return bar
    }

    private fun createLeftTools(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(
                Color.rgb(14, 17, 25)
            )
            setPadding(
                5.dp(),
                8.dp(),
                5.dp(),
                8.dp()
            )
        }

        addTool(bar, "Media")
        addTool(bar, "Layer")
        addTool(bar, "Text")
        addTool(bar, "Audio")
        addTool(bar, "Voice")

        return bar
    }

    private fun createRightTools(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(
                Color.rgb(14, 17, 25)
            )
            setPadding(
                5.dp(),
                8.dp(),
                5.dp(),
                8.dp()
            )
        }

        addTool(bar, "Crop")
        addTool(bar, "Speed")
        addTool(bar, "Filter")
        addTool(bar, "Effect")
        addTool(bar, "More")

        return bar
    }

    private fun addTool(
        parent: LinearLayout,
        name: String
    ) {

        val button = TextView(this).apply {
            text = name
            setTextColor(Color.WHITE)
            textSize = 10f
            gravity = Gravity.CENTER
            setBackgroundColor(
                Color.rgb(25, 29, 42)
            )
        }

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                45.dp()
            )

        params.setMargins(
            2.dp(),
            3.dp(),
            2.dp(),
            3.dp()
        )

        parent.addView(
            button,
            params
        )
    }

    private fun createTimeline(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(
                Color.rgb(9, 12, 19)
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(
                Color.rgb(17, 20, 30)
            )
        }

        val timelineTitle = TextView(this).apply {
            text = "Timeline"
            setTextColor(Color.WHITE)
            textSize = 13f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                14.dp(),
                0,
                0,
                0
            )
        }

        header.addView(
            timelineTitle,
            LinearLayout.LayoutParams(
                0,
                40.dp(),
                1f
            )
        )

        val minus = toolButton("−", 18)
        val zoom = TextView(this).apply {
            text = "100%"
            setTextColor(Color.LTGRAY)
            textSize = 10f
            gravity = Gravity.CENTER
        }
        val plus = toolButton("+", 18)

        header.addView(
            minus,
            LinearLayout.LayoutParams(
                42.dp(),
                40.dp()
            )
        )

        header.addView(
            zoom,
            LinearLayout.LayoutParams(
                50.dp(),
                40.dp()
            )
        )

        header.addView(
            plus,
            LinearLayout.LayoutParams(
                42.dp(),
                40.dp()
            )
        )

        container.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                40.dp()
            )
        )

        val scroll =
            android.widget.HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
            }

        timelineTrack = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                120.dp(),
                10.dp(),
                120.dp(),
                10.dp()
            )
        }

        if (sourceType == "video") {

            val clip = TextView(this).apply {
                text = "VIDEO"
                setTextColor(Color.WHITE)
                textSize = 11f
                gravity = Gravity.CENTER
                setBackgroundColor(
                    Color.rgb(50, 82, 140)
                )
            }

            timelineTrack.addView(
                clip,
                LinearLayout.LayoutParams(
                    500.dp(),
                    70.dp()
                )
            )
        }

        scroll.addView(
            timelineTrack,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        container.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                140.dp()
            )
        )

        return container
    }

    private fun toolButton(
        value: String,
        size: Int
    ): TextView {

        return TextView(this).apply {
            text = value
            setTextColor(Color.WHITE)
            textSize = size.toFloat()
            gravity = Gravity.CENTER
        }
    }

    private fun Int.dp(): Int {

        return (
            this *
                resources.displayMetrics.density
            ).toInt()
    }
}
