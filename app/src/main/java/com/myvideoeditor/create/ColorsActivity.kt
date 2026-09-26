package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
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

            "Red" to Color.rgb(
                244,
                67,
                54
            ),

            "Blue" to Color.rgb(
                33,
                150,
                243
            ),

            "Green" to Color.rgb(
                46,
                204,
                113
            ),

            "Yellow" to Color.rgb(
                255,
                193,
                7
            ),

            "Purple" to Color.rgb(
                156,
                39,
                176
            ),

            "Pink" to Color.rgb(
                233,
                30,
                99
            ),

            "Orange" to Color.rgb(
                255,
                152,
                0
            ),

            "Cyan" to Color.rgb(
                0,
                188,
                212
            ),

            "Deep Blue" to Color.rgb(
                63,
                81,
                181
            ),

            "Brown" to Color.rgb(
                121,
                85,
                72
            )
        )

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
            item in colors
        ) {

            val name =
                item.first

            val color =
                item.second

            val card =
                createColorFolder(
                    name,
                    color
                )

            val params =
                GridLayout.LayoutParams().apply {

                    width = 0

                    height = dp(
                        118
                    )

                    columnSpec =
                        GridLayout.spec(
                            GridLayout.UNDEFINED,
                            1f
                        )

                    setMargins(
                        dp(5),
                        dp(5),
                        dp(5),
                        dp(5)
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
    }

    private fun createColorFolder(
        name: String,
        color: Int
    ): FrameLayout {

        val frame =
            FrameLayout(this)

        frame.setPadding(
            dp(0),
            dp(18),
            dp(0),
            dp(0)
        )

        /*
         * Main folder body
         */

        val body =
            TextView(this).apply {

                gravity =
                    Gravity.CENTER

                text =
                    name

                textSize =
                    15f

                setTextColor(
                    getTextColor(
                        color
                    )
                )

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                background =
                    roundedBackground(
                        color,
                        12
                    )
            }

        frame.addView(
            body,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        /*
         * Folder top tab
         */

        val tab =
            View(this)

        tab.background =
            roundedTopBackground(
                color,
                14
            )

        val tabParams =
            FrameLayout.LayoutParams(
                dp(58),
                dp(26)
            )

        tabParams.gravity =
            Gravity.TOP or
                Gravity.START

        frame.addView(
            tab,
            tabParams
        )

        /*
         * Click
         */

        frame.setOnClickListener {

            selectColor(
                name,
                color
            )
        }

        return frame
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadius =
                dp(radius).toFloat()
        }
    }

    private fun roundedTopBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                darkenColor(
                    color
                )
            )

            cornerRadii =
                floatArrayOf(
                    dp(radius).toFloat(),
                    dp(radius).toFloat(),
                    dp(radius).toFloat(),
                    dp(radius).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
                )
        }
    }

    private fun darkenColor(
        color: Int
    ): Int {

        val red =
            (Color.red(color) * 0.82f)
                .toInt()

        val green =
            (Color.green(color) * 0.82f)
                .toInt()

        val blue =
            (Color.blue(color) * 0.82f)
                .toInt()

        return Color.rgb(
            red,
            green,
            blue
        )
    }

    private fun getTextColor(
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

    private fun selectColor(
        name: String,
        color: Int
    ) {

        val result =
            Intent().apply {

                putExtra(
                    EXTRA_COLOR,
                    color
                )

                putExtra(
                    EXTRA_COLOR_NAME,
                    name
                )
            }

        setResult(
            RESULT_OK,
            result
        )

        finish()
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
