package com.myvideoeditor.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.myvideoeditor.R

class ColorsActivity : Activity() {

    companion object {

        const val EXTRA_COLOR = "selected_color"

        const val EXTRA_COLOR_NAME =
            "selected_color_name"

        private const val IMAGE_PERMISSION_REQUEST =
            701
    }

    private val colors =
        listOf(
            "Black" to Color.BLACK,
            "White" to Color.WHITE,
            "Red" to Color.rgb(244, 67, 54),
            "Blue" to Color.rgb(33, 150, 243),
            "Green" to Color.rgb(46, 204, 113),
            "Yellow" to Color.rgb(255, 193, 7),
            "Purple" to Color.rgb(156, 39, 176),
            "Pink" to Color.rgb(233, 30, 99),
            "Orange" to Color.rgb(255, 152, 0),
            "Cyan" to Color.rgb(0, 188, 212),
            "Deep Blue" to Color.rgb(63, 81, 181),
            "Brown" to Color.rgb(121, 85, 72)
        )

    private val borderColors =
        listOf(
            Color.rgb(0, 188, 212),
            Color.rgb(255, 152, 0),
            Color.rgb(233, 30, 99),
            Color.rgb(156, 39, 176),
            Color.rgb(33, 150, 243),
            Color.rgb(76, 175, 80),
            Color.rgb(255, 193, 7),
            Color.rgb(244, 67, 54),
            Color.rgb(0, 230, 118),
            Color.rgb(255, 64, 129),
            Color.rgb(124, 77, 255),
            Color.rgb(3, 169, 244)
        )

    private val checkViews =
        ArrayList<TextView>()

    private var selectedIndex =
        -1

    private var selectedColor =
        Color.BLACK

    private var selectedColorName =
        ""

    private lateinit var colorsGrid: GridLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_colors
        )

        colorsGrid =
            findViewById(
                R.id.colorsGrid
            )

        createSolidColors()

        findViewById<View>(
            R.id.colorsBack
        ).setOnClickListener {
            finish()
        }

        findViewById<TextView>(
            R.id.colorsContinue
        ).setOnClickListener {
            continueWithColor()
        }

        updateSelection()

        loadColorBackgrounds()
    }

    private fun createSolidColors() {

        for (index in colors.indices) {

            val name =
                colors[index].first

            val color =
                colors[index].second

            val card =
                createColorFolder(
                    index = index,
                    name = name,
                    color = color
                )

            val params =
                GridLayout.LayoutParams().apply {

                    width = 0

                    height = dp(100)

                    columnSpec =
                        GridLayout.spec(
                            GridLayout.UNDEFINED,
                            1f
                        )

                    setMargins(
                        dp(4),
                        dp(4),
                        dp(4),
                        dp(4)
                    )
                }

            colorsGrid.addView(
                card,
                params
            )
        }
    }

    private fun loadColorBackgrounds() {

        val title =
            TextView(this).apply {

                text =
                    "COLOR BACKGROUNDS"

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

                textSize =
                    22f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                includeFontPadding =
                    false

                setPadding(
                    0,
                    dp(18),
                    0,
                    dp(10)
                )
            }

        val titleParams =
            GridLayout.LayoutParams().apply {

                width =
                    ViewGroup.LayoutParams.MATCH_PARENT

                height =
                    dp(56)

                columnSpec =
                    GridLayout.spec(
                        0,
                        3
                    )
            }

        colorsGrid.addView(
            title,
            titleParams
        )

        val backgrounds =
            getColorBackgrounds()

        if (backgrounds.isEmpty()) {

            val emptyText =
                TextView(this).apply {

                    text =
                        "No color backgrounds found"

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        Color.LTGRAY
                    )

                    textSize =
                        15f

                    includeFontPadding =
                        false
                }

            val emptyParams =
                GridLayout.LayoutParams().apply {

                    width =
                        ViewGroup.LayoutParams.MATCH_PARENT

                    height =
                        dp(50)

                    columnSpec =
                        GridLayout.spec(
                            0,
                            3
                        )
                }

            colorsGrid.addView(
                emptyText,
                emptyParams
            )

            return
        }

        for (index in backgrounds.indices) {

            val uri =
                backgrounds[index]

            val image =
                createBackgroundImage(
                    uri = uri,
                    index = index
                )

            val params =
                GridLayout.LayoutParams().apply {

                    width = 0

                    height = dp(82)

                    columnSpec =
                        GridLayout.spec(
                            GridLayout.UNDEFINED,
                            1f
                        )

                    setMargins(
                        dp(4),
                        dp(4),
                        dp(4),
                        dp(4)
                    )
                }

            colorsGrid.addView(
                image,
                params
            )
        }
    }

    private fun createBackgroundImage(
        uri: Uri,
        index: Int
    ): ImageView {

        val image =
            ImageView(this).apply {

                scaleType =
                    ImageView.ScaleType.CENTER_CROP

                setImageURI(
                    uri
                )

                background =
                    createBackgroundBorder(
                        borderColors[
                            index %
                                borderColors.size
                        ]
                    )

                setPadding(
                    dp(1),
                    dp(1),
                    dp(1),
                    dp(1)
                )

                contentDescription =
                    "Color background ${index + 1}"
            }

        image.setOnClickListener {

            selectedIndex = -1

            selectedColor =
                Color.BLACK

            selectedColorName =
                ""

            updateSelection()

            Toast.makeText(
                this,
                "Background selected",
                Toast.LENGTH_SHORT
            ).show()
        }

        return image
    }

    private fun getColorBackgrounds():
        ArrayList<Uri> {

        val result =
            ArrayList<Uri>()

        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection =
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
            )

        val selection =
            MediaStore.Images.Media.RELATIVE_PATH +
                " LIKE ?"

        val selectionArgs =
            arrayOf(
                "KCEDITOR_COLOR_BACKGROUNDS/%"
            )

        val sortOrder =
            MediaStore.Images.Media.DISPLAY_NAME +
                " ASC"

        try {

            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(
                            idColumn
                        )

                    val uri =
                        Uri.withAppendedPath(
                            collection,
                            id.toString()
                        )

                    result.add(
                        uri
                    )
                }
            }

        } catch (
            exception: Exception
        ) {

            exception.printStackTrace()
        }

        return result
    }

    private fun createColorFolder(
        index: Int,
        name: String,
        color: Int
    ): FrameLayout {

        val frame =
            FrameLayout(this)

        val body =
            FrameLayout(this).apply {

                background =
                    createFolderBody(
                        color
                    )
            }

        val bodyParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(82)
            )

        bodyParams.topMargin =
            dp(18)

        frame.addView(
            body,
            bodyParams
        )

        val tab =
            View(this).apply {

                background =
                    createFolderTab(
                        color
                    )
            }

        val tabParams =
            FrameLayout.LayoutParams(
                dp(48),
                dp(24)
            )

        tabParams.gravity =
            Gravity.TOP or
                Gravity.START

        frame.addView(
            tab,
            tabParams
        )

        val title =
            TextView(this).apply {

                gravity =
                    Gravity.CENTER

                text =
                    name

                textSize =
                    13f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    getReadableTextColor(
                        color
                    )
                )

                maxLines =
                    1
            }

        val titleParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        titleParams.topMargin =
            dp(18)

        body.addView(
            title,
            titleParams
        )

        val check =
            TextView(this).apply {

                gravity =
                    Gravity.CENTER

                text =
                    "✓"

                setTextColor(
                    Color.WHITE
                )

                textSize =
                    16f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                background =
                    createCheckBackground()

                visibility =
                    View.GONE
            }

        val checkParams =
            FrameLayout.LayoutParams(
                dp(28),
                dp(28)
            )

        checkParams.gravity =
            Gravity.TOP or
                Gravity.END

        checkParams.setMargins(
            0,
            dp(20),
            dp(5),
            0
        )

        frame.addView(
            check,
            checkParams
        )

        checkViews.add(
            check
        )

        frame.setOnClickListener {

            if (selectedIndex == index) {

                selectedIndex =
                    -1

                selectedColor =
                    Color.BLACK

                selectedColorName =
                    ""

            } else {

                selectedIndex =
                    index

                selectedColor =
                    color

                selectedColorName =
                    name
            }

            updateSelection()
        }

        return frame
    }

    private fun updateSelection() {

        for (
            index in
            checkViews.indices
        ) {

            checkViews[index].visibility =
                if (
                    index ==
                    selectedIndex
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun continueWithColor() {

        if (
            selectedIndex ==
            -1
        ) {

            Toast.makeText(
                this,
                "Please select a background color",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val result =
            Intent().apply {

                putExtra(
                    EXTRA_COLOR,
                    selectedColor
                )

                putExtra(
                    EXTRA_COLOR_NAME,
                    selectedColorName
                )
            }

        setResult(
            RESULT_OK,
            result
        )

        finish()
    }

    private fun createFolderBody(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadii =
                floatArrayOf(
                    0f,
                    0f,
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    dp(5).toFloat()
                )
        }
    }

    private fun createFolderTab(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadii =
                floatArrayOf(
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
                )
        }
    }

    private fun createBackgroundBorder(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                Color.TRANSPARENT
            )

            setStroke(
                dp(1),
                color
            )
        }
    }

    private fun createCheckBackground():
        GradientDrawable {

        return GradientDrawable().apply {

            shape =
                GradientDrawable.OVAL

            setColor(
                Color.rgb(
                    20,
                    190,
                    95
                )
            )

            setStroke(
                dp(2),
                Color.WHITE
            )
        }
    }

    private fun getReadableTextColor(
        color: Int
    ): Int {

        val brightness =
            (
                Color.red(color) * 299 +
                    Color.green(color) * 587 +
                    Color.blue(color) * 114
                ) / 1000

        return if (
            brightness > 165
        ) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
