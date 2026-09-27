package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import com.myvideoeditor.R

class ColorsActivity : Activity() {

    companion object {

        const val EXTRA_COLOR =
            "selected_color"

        const val EXTRA_COLOR_NAME =
            "selected_color_name"
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

    private var selectedIndex =
        0

    private var selectedColor =
        Color.BLACK

    private var selectedColorName =
        "Black"

    private val checkViews =
        ArrayList<TextView>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_colors
        )

        val grid =
            findViewById<GridLayout>(
                R.id.colorsGrid
            )

        for (
            index in colors.indices
        ) {

            val name =
                colors[index].first

            val color =
                colors[index].second

            val card =
                createColorFolder(
                    index,
                    name,
                    color
                )

            val params =
                GridLayout.LayoutParams().apply {

                    width =
                        0

                    height =
                        dp(100)

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

            grid.addView(
                card,
                params
            )
        }

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
    }

    private fun createColorFolder(
        index: Int,
        name: String,
        color: Int
    ): FrameLayout {

        val frame =
            FrameLayout(
                this
            )

        val body =
            FrameLayout(
                this
            ).apply {

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
            View(
                this
            ).apply {

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
            TextView(
                this
            ).apply {

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
            TextView(
                this
            ).apply {

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

            selectedIndex =
                index

            selectedColor =
                color

            selectedColorName =
                name

            updateSelection()
        }

        return frame
    }

    private fun updateSelection() {

        for (
            index in checkViews.indices
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
