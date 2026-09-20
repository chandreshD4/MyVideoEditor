package com.myvideoeditor.editor

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
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

    private lateinit var previewContainer: FrameLayout
    private lateinit var timelineScroll: HorizontalScrollView
    private lateinit var timelineContent: LinearLayout

    private var aspectRatio = "9:16"
    private var sourceType = "blank"
    private var backgroundColor = Color.WHITE
    private var mediaUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        aspectRatio =
            intent.getStringExtra(EXTRA_ASPECT_RATIO) ?: "9:16"

        sourceType =
            intent.getStringExtra(EXTRA_SOURCE_TYPE) ?: "blank"

        backgroundColor =
            intent.getIntExtra(
                EXTRA_BACKGROUND_COLOR,
                Color.WHITE
            )

        mediaUri =
            intent.getStringExtra(EXTRA_VIDEO_URI)

        buildEditor()
    }

    private fun buildEditor() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 9, 16))
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
            setBackgroundColor(Color.rgb(10, 12, 20))
        }

        workspace.addView(
            createToolBar(true),
            LinearLayout.LayoutParams(
                62.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        previewContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        val previewHolder = FrameLayout(this)

        setupPreview(previewHolder)

        previewContainer.addView(
            previewHolder,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        workspace.addView(
            previewContainer,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        workspace.addView(
            createToolBar(false),
            LinearLayout.LayoutParams(
                62.dp(),
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
            createTimelineArea(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                220.dp()
            )
        )

        setContentView(root)
    }

    private fun createTopBar(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(Gravity.CENTER_VERTICAL)
            setBackgroundColor(Color.rgb(15, 18, 30))
            setPadding(10.dp(), 0, 10.dp(), 0)
        }

        val back = button("‹", 28)

        back.setOnClickListener {
            finish()
        }

        bar.addView(
            back,
            LinearLayout.LayoutParams(
                48.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val title = TextView(this).apply {
            text = "New Project"
            setTextColor(Color.WHITE)
            textSize = 16f
            setGravity(Gravity.CENTER_VERTICAL)
        }

        bar.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        val undo = button("↶", 20)
        val redo = button("↷", 20)

        bar.addView(
            undo,
            LinearLayout.LayoutParams(
                48.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        bar.addView(
            redo,
            LinearLayout.LayoutParams(
                48.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val export = TextView(this).apply {
            text = "EXPORT"
            setTextColor(Color.WHITE)
            textSize = 13f
            setGravity(Gravity.CENTER)
            setPadding(14.dp(), 0, 14.dp(), 0)
            background = rounded(
                Color.rgb(70, 80, 220),
                10
            )
        }

        bar.addView(
            export,
            LinearLayout.LayoutParams(
                90.dp(),
                38.dp()
            )
        )

        return bar
    }

    private fun setupPreview(holder: FrameLayout) {

        when (sourceType) {

            "video" -> {
                val video = VideoView(this)

                if (!mediaUri.isNullOrEmpty()) {
                    video.setVideoURI(Uri.parse(mediaUri))
                }

                holder.addView(
                    video,
                    centeredPreviewParams()
                )
            }

            "image" -> {
                val image = ImageView(this).apply {
                    if (!mediaUri.isNullOrEmpty()) {
                        setImageURI(Uri.parse(mediaUri))
                    }
                    scaleType =
                        ImageView.ScaleType.FIT_CENTER
                }

                holder.addView(
                    image,
                    centeredPreviewParams()
                )
            }

            else -> {
                holder.setBackgroundColor(
                    backgroundColor
                )

                val text = TextView(this).apply {
                    text = aspectRatio
                    setTextColor(Color.GRAY)
                    textSize = 13f
                    setGravity(Gravity.CENTER)
                }

                holder.addView(
                    text,
                    centeredPreviewParams()
                )
            }
        }
    }

    private fun centeredPreviewParams():
        FrameLayout.LayoutParams {

        return FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun createToolBar(left: Boolean): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setGravity(Gravity.CENTER)
            setBackgroundColor(Color.rgb(13, 16, 27))
            setPadding(5.dp(), 8.dp(), 5.dp(), 8.dp())
        }

        if (left) {
            addTool(bar, "Media")
            addTool(bar, "Layer")
            addTool(bar, "Text")
            addTool(bar, "Audio")
            addTool(bar, "Voice")
        } else {
            addTool(bar, "Crop")
            addTool(bar, "Speed")
            addTool(bar, "Filter")
            addTool(bar, "Effect")
            addTool(bar, "More")
        }

        return bar
    }

    private fun addTool(
        parent: LinearLayout,
        name: String
    ) {

        val item = TextView(this).apply {
            text = name
            setTextColor(Color.WHITE)
            textSize = 10f
            setGravity(Gravity.CENTER)
            background = rounded(
                Color.rgb(24, 28, 43),
                8
            )
        }

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48.dp()
            )

        params.setMargins(
            2.dp(),
            4.dp(),
            2.dp(),
            4.dp()
        )

        parent.addView(item, params)
    }

    private fun createTimelineArea(): View {

        val area = FrameLayout(this).apply {
            setBackgroundColor(
                Color.rgb(12, 15, 25)
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(Gravity.CENTER)
            setBackgroundColor(
                Color.rgb(18, 22, 35)
            )
        }

        val zoomOut = button("−", 20)

        val zoomText = TextView(this).apply {
            text = "100%"
            setTextColor(Color.WHITE)
            setGravity(Gravity.CENTER)
            textSize = 11f
        }

        val zoomIn = button("+", 20)

        header.addView(
            zoomOut,
            LinearLayout.LayoutParams(
                45.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        header.addView(
            zoomText,
            LinearLayout.LayoutParams(
                60.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        header.addView(
            zoomIn,
            LinearLayout.LayoutParams(
                45.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        area.addView(
            header,
            FrameLayout.LayoutParams(
                150.dp(),
                40.dp()
            )
        )

        timelineScroll =
            HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(
                    Color.rgb(9, 12, 20)
                )
            }

        timelineContent =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    180.dp(),
                    8.dp(),
                    180.dp(),
                    8.dp()
                )
            }

        addTimelineTrack(
            "VIDEO",
            Color.rgb(45, 75, 125)
        )

        addTimelineTrack(
            "TEXT",
            Color.rgb(80, 55, 105)
        )

        addTimelineTrack(
            "AUDIO",
            Color.rgb(45, 105, 75)
        )

        timelineScroll.addView(
            timelineContent,
            ViewGroup.LayoutParams(
                1800.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val scrollParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        scrollParams.topMargin = 40.dp()

        area.addView(
            timelineScroll,
            scrollParams
        )

        val playhead = View(this).apply {
            setBackgroundColor(Color.RED)
        }

        val playheadParams =
            FrameLayout.LayoutParams(
                2.dp(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        playheadParams.gravity =
            Gravity.CENTER_HORIZONTAL

        playheadParams.topMargin = 40.dp()

        area.addView(
            playhead,
            playheadParams
        )

        zoomOut.setOnClickListener {
            zoomText.text = "75%"
        }

        zoomIn.setOnClickListener {
            zoomText.text = "125%"
        }

        return area
    }

    private fun addTimelineTrack(
        name: String,
        color: Int
    ) {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(Gravity.CENTER_VERTICAL)
        }

        val label = TextView(this).apply {
            text = name
            setTextColor(Color.WHITE)
            textSize = 10f
            setGravity(Gravity.CENTER)
            background = rounded(
                Color.rgb(25, 29, 42),
                6
            )
        }

        row.addView(
            label,
            LinearLayout.LayoutParams(
                80.dp(),
                48.dp()
            )
        )

        val clip = TextView(this).apply {
            text =
                if (name == "VIDEO") {
                    "  Video Clip  "
                } else {
                    "  $name Track  "
                }

            setTextColor(Color.WHITE)
            textSize = 12f
            setGravity(Gravity.CENTER_VERTICAL)
            background = rounded(
                color,
                8
            )
        }

        val clipParams =
            LinearLayout.LayoutParams(
                520.dp(),
                48.dp()
            )

        clipParams.setMargins(
            12.dp(),
            0,
            0,
            0
        )

        row.addView(
            clip,
            clipParams
        )

        val params =
            LinearLayout.LayoutParams(
                1800.dp(),
                52.dp()
            )

        timelineContent.addView(
            row,
            params
        )
    }

    private fun button(
        textValue: String,
        size: Int
    ): TextView {

        return TextView(this).apply {
            text = textValue
            setTextColor(Color.WHITE)
            textSize = size.toFloat()
            setGravity(Gravity.CENTER)
        }
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius =
                radius.dp().toFloat()
        }
    }

    private fun Int.dp(): Int {
        return (
            this *
                resources.displayMetrics.density
            ).toInt()
    }
}
