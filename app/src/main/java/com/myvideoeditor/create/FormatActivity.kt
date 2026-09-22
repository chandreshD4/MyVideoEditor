package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.myvideoeditor.R

class FormatActivity : Activity() {

    companion object {
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_PROJECT_NAME = "project_name"
    }

    private var selectedRatio = "16:9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_format_selection)

        val projectName =
            findViewById<EditText>(R.id.projectName)

        findViewById<TextView>(R.id.formatBack)
            .setOnClickListener {
                finish()
            }

        setupRatio(
            R.id.formatYouTube,
            "16:9"
        )

        setupRatio(
            R.id.formatShorts,
            "9:16"
        )

        setupRatio(
            R.id.formatSquare,
            "1:1"
        )

        setupRatio(
            R.id.format43,
            "4:3"
        )

        setupRatio(
            R.id.format34,
            "3:4"
        )

        setupRatio(
            R.id.format45,
            "4:5"
        )

        setupRatio(
            R.id.formatCinema,
            "2.35:1"
        )

        val photoDuration =
            findViewById<SeekBar>(
                R.id.photoDurationSeek
            )

        val photoDurationValue =
            findViewById<TextView>(
                R.id.photoDurationValue
            )

        photoDuration.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val value =
                        1.0 + (progress / 10.0)

                    photoDurationValue.text =
                        String.format(
                            "%.1f",
                            value
                        )
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )

        val transitionDuration =
            findViewById<SeekBar>(
                R.id.transitionDurationSeek
            )

        val transitionDurationValue =
            findViewById<TextView>(
                R.id.transitionDurationValue
            )

        transitionDuration.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val value =
                        0.5 + (progress / 10.0)

                    transitionDurationValue.text =
                        String.format(
                            "%.1f",
                            value
                        )
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )

        findViewById<TextView>(R.id.formatCreate)
            .setOnClickListener {

                val name =
                    projectName.text
                        .toString()
                        .trim()
                        .ifEmpty {
                            "New Project"
                        }

                val intent =
                    Intent(
                        this,
                        MediaSourceActivity::class.java
                    ).apply {

                        putExtra(
                            EXTRA_ASPECT_RATIO,
                            selectedRatio
                        )

                        putExtra(
                            EXTRA_PROJECT_NAME,
                            name
                        )
                    }

                startActivity(intent)
            }
    }

    private fun setupRatio(
        viewId: Int,
        ratio: String
    ) {

        findViewById<LinearLayout>(viewId)
            .setOnClickListener {

                selectedRatio = ratio

                updateSelection()
            }
    }

    private fun updateSelection() {

        val selected =
            android.graphics.Color.rgb(
                32,
                37,
                51
            )

        val normal =
            android.graphics.Color.TRANSPARENT

        findViewById<LinearLayout>(
            R.id.formatYouTube
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.formatShorts
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.formatSquare
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.format43
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.format34
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.format45
        ).setBackgroundColor(normal)

        findViewById<LinearLayout>(
            R.id.formatCinema
        ).setBackgroundColor(normal)

        val selectedId =
            when (selectedRatio) {
                "16:9" -> R.id.formatYouTube
                "9:16" -> R.id.formatShorts
                "1:1" -> R.id.formatSquare
                "4:3" -> R.id.format43
                "3:4" -> R.id.format34
                "4:5" -> R.id.format45
                "2.35:1" -> R.id.formatCinema
                else -> R.id.formatYouTube
            }

        findViewById<LinearLayout>(
            selectedId
        ).setBackgroundColor(selected)
    }
}
