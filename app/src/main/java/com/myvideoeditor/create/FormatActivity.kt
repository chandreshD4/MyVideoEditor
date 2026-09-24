package com.myvideoeditor.create

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.myvideoeditor.R

class FormatActivity : Activity() {

    companion object {
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_PHOTO_MODE = "photo_mode"
        const val EXTRA_PHOTO_DURATION = "photo_duration"
        const val EXTRA_TRANSITION_DURATION = "transition_duration"
    }

    private var selectedRatio = "16:9"
    private var customRatio = false

    private var photoMode = "Fit"
    private var photoDuration = 4.5f
    private var transitionDuration = 1.5f

    private lateinit var advancedContent: View
    private lateinit var advancedArrow: TextView
    private lateinit var customRatioView: TextView

    private lateinit var ratio16x9: TextView
    private lateinit var ratio9x16: TextView
    private lateinit var ratio1x1: TextView
    private lateinit var ratio4x3: TextView
    private lateinit var ratio3x4: TextView
    private lateinit var ratio4x5: TextView
    private lateinit var ratioCinema: TextView

    private lateinit var photoFit: TextView
    private lateinit var photoFill: TextView
    private lateinit var photoAuto: TextView

    private lateinit var photoDurationValue: TextView
    private lateinit var transitionDurationValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_format_selection)

        bindViews()
        setupClicks()
        setupSliders()

        updateRatioSelection()
        updatePhotoModeSelection()
    }

    private fun bindViews() {

        advancedArrow =
            findViewById(R.id.advancedArrow)

        advancedContent =
            findViewById(R.id.advancedContent)

        customRatioView =
            findViewById(R.id.customRatio)

        ratio16x9 =
            findViewById(R.id.ratio16x9)

        ratio9x16 =
            findViewById(R.id.ratio9x16)

        ratio1x1 =
            findViewById(R.id.ratio1x1)

        ratio4x3 =
            findViewById(R.id.ratio4x3)

        ratio3x4 =
            findViewById(R.id.ratio3x4)

        ratio4x5 =
            findViewById(R.id.ratio4x5)

        ratioCinema =
            findViewById(R.id.ratioCinema)

        photoFit =
            findViewById(R.id.photoFit)

        photoFill =
            findViewById(R.id.photoFill)

        photoAuto =
            findViewById(R.id.photoAuto)

        photoDurationValue =
            findViewById(R.id.photoDurationValue)

        transitionDurationValue =
            findViewById(R.id.transitionDurationValue)
    }

    private fun setupClicks() {

        findViewById<TextView>(R.id.formatClose)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.importButton)
            .setOnClickListener {
                openImportPicker()
            }

        ratio16x9.setOnClickListener {
            chooseRatio("16:9")
        }

        ratio9x16.setOnClickListener {
            chooseRatio("9:16")
        }

        ratio1x1.setOnClickListener {
            chooseRatio("1:1")
        }

        ratio4x3.setOnClickListener {
            chooseRatio("4:3")
        }

        ratio3x4.setOnClickListener {
            chooseRatio("3:4")
        }

        ratio4x5.setOnClickListener {
            chooseRatio("4:5")
        }

        ratioCinema.setOnClickListener {
            chooseRatio("2.35:1")
        }

        customRatioView.setOnClickListener {
            showCustomSizeDialog()
        }

        findViewById<View>(R.id.advancedHeader)
            .setOnClickListener {
                toggleAdvanced()
            }

        photoFit.setOnClickListener {
            photoMode = "Fit"
            updatePhotoModeSelection()
        }

        photoFill.setOnClickListener {
            photoMode = "Fill"
            updatePhotoModeSelection()
        }

        photoAuto.setOnClickListener {
            photoMode = "Auto"
            updatePhotoModeSelection()
        }

        findViewById<TextView>(R.id.createButton)
            .setOnClickListener {
                createProject()
            }
    }

    private fun setupSliders() {

        val photoSlider =
            findViewById<SeekBar>(
                R.id.photoDurationSlider
            )

        val transitionSlider =
            findViewById<SeekBar>(
                R.id.transitionDurationSlider
            )

        photoSlider.progress = 35
        transitionSlider.progress = 15

        photoSlider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    photoDuration =
                        1.0f + (progress / 10.0f)

                    photoDurationValue.text =
                        String.format(
                            "%.1f",
                            photoDuration
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

        transitionSlider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    transitionDuration =
                        0.5f + (progress / 10.0f)

                    transitionDurationValue.text =
                        String.format(
                            "%.1f",
                            transitionDuration
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

        photoDurationValue.text = "4.5"
        transitionDurationValue.text = "1.5"
    }

    private fun toggleAdvanced() {

        if (advancedContent.visibility == View.VISIBLE) {

            advancedContent.visibility =
                View.GONE

            advancedArrow.text = "⌄"

        } else {

            advancedContent.visibility =
                View.VISIBLE

            advancedArrow.text = "⌃"
        }
    }

    private fun chooseRatio(
        ratio: String
    ) {

        selectedRatio = ratio
        customRatio = false

        customRatioView.text =
            "+\nCUSTOM"

        updateRatioSelection()
    }

    private fun updateRatioSelection() {

        val all = listOf(
            ratio16x9,
            ratio9x16,
            ratio1x1,
            ratio4x3,
            ratio3x4,
            ratio4x5,
            ratioCinema,
            customRatioView
        )

        for (item in all) {

            item.setBackgroundResource(
                R.drawable.bg_ratio_unselected
            )

            item.setTextColor(
                Color.rgb(
                    190,
                    195,
                    205
                )
            )

            item.alpha = 0.85f
        }

        val selected: TextView =
            if (customRatio) {

                customRatioView

            } else {

                when (selectedRatio) {

                    "16:9" -> ratio16x9
                    "9:16" -> ratio9x16
                    "1:1" -> ratio1x1
                    "4:3" -> ratio4x3
                    "3:4" -> ratio3x4
                    "4:5" -> ratio4x5
                    "2.35:1" -> ratioCinema

                    else -> ratio16x9
                }
            }

        selected.setBackgroundResource(
            R.drawable.bg_ratio_selected
        )

        selected.setTextColor(
            Color.rgb(
                255,
                85,
                95
            )
        )

        selected.alpha = 1.0f
    }

    private fun updatePhotoModeSelection() {

        val all = listOf(
            photoFit,
            photoFill,
            photoAuto
        )

        for (item in all) {

            item.setBackgroundResource(
                R.drawable.bg_mode_unselected
            )

            item.setTextColor(
                Color.rgb(
                    185,
                    190,
                    200
                )
            )
        }

        val selected =
            when (photoMode) {

                "Fill" -> photoFill
                "Auto" -> photoAuto

                else -> photoFit
            }

        selected.setBackgroundResource(
            R.drawable.bg_mode_selected
        )

        selected.setTextColor(
            Color.rgb(
                65,
                205,
                245
            )
        )
    }

    private fun createProject() {

        val name =
            findViewById<EditText>(
                R.id.projectName
            )
                .text
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

                putExtra(
                    EXTRA_PHOTO_MODE,
                    photoMode
                )

                putExtra(
                    EXTRA_PHOTO_DURATION,
                    photoDuration
                )

                putExtra(
                    EXTRA_TRANSITION_DURATION,
                    transitionDuration
                )
            }

        startActivity(intent)
    }

    private fun openImportPicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "*/*"

                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "video/*",
                        "image/*"
                    )
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            501
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode != 501 ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri: Uri =
            data?.data ?: return

        try {

            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

        } catch (_: SecurityException) {
        }

        val mime =
            contentResolver.getType(uri) ?: ""

        val sourceType =
            if (mime.startsWith("video/")) {
                "video"
            } else {
                "image"
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
                    "Imported Project"
                )

                putExtra(
                    MediaSourceActivity.EXTRA_SOURCE_TYPE,
                    sourceType
                )

                putExtra(
                    MediaSourceActivity.EXTRA_MEDIA_URI,
                    uri.toString()
                )
            }

        startActivity(intent)
    }

    private fun showCustomSizeDialog() {

        val layout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(24),
                    dp(8),
                    dp(24),
                    dp(4)
                )
            }

        val widthInput =
            EditText(this).apply {

                hint = "Width (px)"
                inputType =
                    InputType.TYPE_CLASS_NUMBER

                textSize = 16f
                isSingleLine = true
                minHeight = dp(52)

                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    0
                )
            }

        val heightInput =
            EditText(this).apply {

                hint = "Height (px)"
                inputType =
                    InputType.TYPE_CLASS_NUMBER

                textSize = 16f
                isSingleLine = true
                minHeight = dp(52)

                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    0
                )
            }

        layout.addView(
            widthInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {

                bottomMargin =
                    dp(10)
            }
        )

        layout.addView(
            heightInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "Custom Canvas Size"
                )
                .setMessage(
                    "Enter width and height in pixels"
                )
                .setView(layout)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "Use Size",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val width =
                    widthInput
                        .text
                        .toString()
                        .trim()
                        .toIntOrNull()

                val height =
                    heightInput
                        .text
                        .toString()
                        .trim()
                        .toIntOrNull()

                if (
                    width == null ||
                    height == null ||
                    width <= 0 ||
                    height <= 0
                ) {

                    if (
                        width == null ||
                        width <= 0
                    ) {
                        widthInput.error =
                            "Enter valid width"
                    }

                    if (
                        height == null ||
                        height <= 0
                    ) {
                        heightInput.error =
                            "Enter valid height"
                    }

                    return@setOnClickListener
                }

                selectedRatio =
                    "${width}×${height}"

                customRatio = true

                customRatioView.text =
                    "✓\n${width}×${height}"

                updateRatioSelection()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources
                    .displayMetrics
                    .density
            ).toInt()
    }
}
