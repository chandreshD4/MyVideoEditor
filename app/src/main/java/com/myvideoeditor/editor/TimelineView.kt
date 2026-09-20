package com.myvideoeditor.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val rulerPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val trackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val trackBorderPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val playheadPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val thumbnailPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private var durationMs = 0
    private var positionMs = 0

    private var pixelsPerSecond = 35f

    private var videoUri: Uri? = null

    private val thumbnails =
        mutableListOf<Bitmap>()

    private var thumbnailGeneration = 0

    private var onPositionChanged:
        ((Int) -> Unit)? = null

    init {

        setBackgroundColor(
            Color.rgb(
                10,
                16,
                41
            )
        )

        rulerPaint.color =
            Color.rgb(
                120,
                130,
                180
            )

        rulerPaint.strokeWidth = 2f

        textPaint.color =
            Color.WHITE

        textPaint.textSize = 22f

        trackPaint.color =
            Color.rgb(
                24,
                38,
                79
            )

        trackBorderPaint.color =
            Color.rgb(
                124,
                0,
                255
            )

        trackBorderPaint.style =
            Paint.Style.STROKE

        trackBorderPaint.strokeWidth = 3f

        thumbnailPaint.isFilterBitmap = true

        playheadPaint.color =
            Color.rgb(
                255,
                30,
                150
            )

        playheadPaint.strokeWidth = 4f

        isClickable = true
    }

    fun setVideoUri(uri: Uri) {

        videoUri = uri

        thumbnailGeneration++

        clearThumbnails()

        invalidate()

        if (durationMs > 0) {
            loadThumbnails()
        }
    }

    fun setDuration(
        duration: Int
    ) {

        durationMs =
            max(
                duration,
                0
            )

        requestLayout()

        invalidate()

        if (
            videoUri != null &&
            durationMs > 0
        ) {
            loadThumbnails()
        }
    }

    fun setPosition(
        position: Int
    ) {

        positionMs =
            position.coerceIn(
                0,
                durationMs
            )

        invalidate()
    }

    fun setOnPositionChangedListener(
        listener: (Int) -> Unit
    ) {

        onPositionChanged = listener
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {

        val durationSeconds =
            max(
                durationMs / 1000f,
                10f
            )

        val desiredWidth =
            (
                durationSeconds *
                    pixelsPerSecond +
                    80f
            ).toInt()

        val width =
            resolveSize(
                desiredWidth,
                widthMeasureSpec
            )

        val height =
            resolveSize(
                210,
                heightMeasureSpec
            )

        setMeasuredDimension(
            width,
            height
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {

        super.onDraw(canvas)

        val width =
            measuredWidth.toFloat()

        val height =
            measuredHeight.toFloat()

        drawRuler(
            canvas,
            width
        )

        drawVideoTrack(
            canvas,
            width
        )

        drawAudioTrack(
            canvas,
            width
        )

        drawPlayhead(
            canvas,
            height
        )
    }

    private fun drawRuler(
        canvas: Canvas,
        width: Float
    ) {

        val rulerY = 38f

        canvas.drawLine(
            0f,
            rulerY,
            width,
            rulerY,
            rulerPaint
        )

        val totalSeconds =
            max(
                durationMs / 1000,
                10
            )

        for (
            second in 0..totalSeconds
        ) {

            val x =
                20f +
                    second *
                    pixelsPerSecond

            if (x > width) {
                break
            }

            val isMajor =
                second % 5 == 0

            val tickHeight =
                if (isMajor) {
                    18f
                } else {
                    9f
                }

            canvas.drawLine(
                x,
                rulerY,
                x,
                rulerY + tickHeight,
                rulerPaint
            )

            if (isMajor) {

                canvas.drawText(
                    formatTime(
                        second * 1000
                    ),
                    x + 4f,
                    24f,
                    textPaint
                )
            }
        }
    }

    private fun drawVideoTrack(
        canvas: Canvas,
        width: Float
    ) {

        val top = 55f
        val bottom = 120f
        val left = 10f

        val right =
            max(
                width - 10f,
                100f
            )

        val rect =
            RectF(
                left,
                top,
                right,
                bottom
            )

        canvas.drawRoundRect(
            rect,
            14f,
            14f,
            trackPaint
        )

        if (thumbnails.isNotEmpty()) {

            val thumbnailWidth =
                (right - left) /
                    thumbnails.size

            for (
                index in thumbnails.indices
            ) {

                val bitmap =
                    thumbnails[index]

                if (bitmap.isRecycled) {
                    continue
                }

                val thumbLeft =
                    left +
                        index *
                        thumbnailWidth

                val thumbRight =
                    if (
                        index ==
                        thumbnails.lastIndex
                    ) {
                        right
                    } else {
                        thumbLeft +
                            thumbnailWidth
                    }

                val destination =
                    RectF(
                        thumbLeft + 2f,
                        top + 2f,
                        thumbRight - 2f,
                        bottom - 2f
                    )

                canvas.drawBitmap(
                    bitmap,
                    null,
                    destination,
                    thumbnailPaint
                )
            }

        } else {

            val clipWidth = 70f
            var x = 15f

            while (
                x < right - 5f
            ) {

                val clipRight =
                    minOf(
                        x + clipWidth,
                        right - 5f
                    )

                val clipRect =
                    RectF(
                        x,
                        top + 5f,
                        clipRight,
                        bottom - 5f
                    )

                canvas.drawRoundRect(
                    clipRect,
                    8f,
                    8f,
                    trackBorderPaint
                )

                x +=
                    clipWidth + 5f
            }
        }

        canvas.drawRoundRect(
            rect,
            14f,
            14f,
            trackBorderPaint
        )

        val labelPaint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        labelPaint.color =
            Color.argb(
                190,
                0,
                0,
                0
            )

        val labelRect =
            RectF(
                16f,
                top + 8f,
                100f,
                top + 32f
            )

        canvas.drawRoundRect(
            labelRect,
            8f,
            8f,
            labelPaint
        )

        textPaint.textSize = 16f

        canvas.drawText(
            "VIDEO",
            28f,
            top + 25f,
            textPaint
        )

        textPaint.textSize = 22f
    }

    private fun drawAudioTrack(
        canvas: Canvas,
        width: Float
    ) {

        val top = 130f
        val bottom = 195f
        val left = 10f

        val right =
            max(
                width - 10f,
                100f
            )

        val rect =
            RectF(
                left,
                top,
                right,
                bottom
            )

        canvas.drawRoundRect(
            rect,
            14f,
            14f,
            trackPaint
        )

        canvas.drawRoundRect(
            rect,
            14f,
            14f,
            trackBorderPaint
        )

        textPaint.textSize = 18f

        canvas.drawText(
            "AUDIO",
            28f,
            top + 40f,
            textPaint
        )

        textPaint.textSize = 22f
    }

    private fun drawPlayhead(
        canvas: Canvas,
        height: Float
    ) {

        val seconds =
            positionMs / 1000f

        val x =
            20f +
                seconds *
                pixelsPerSecond

        canvas.drawLine(
            x,
            0f,
            x,
            height,
            playheadPaint
        )

        canvas.drawCircle(
            x,
            5f,
            8f,
            playheadPaint
        )
    }

    private fun loadThumbnails() {

        val uri =
            videoUri ?: return

        if (durationMs <= 0) {
            return
        }

        val generation =
            thumbnailGeneration

        clearThumbnails()

        invalidate()

        Thread {

            val retriever =
                MediaMetadataRetriever()

            val loaded =
                mutableListOf<Bitmap>()

            try {

                retriever.setDataSource(
                    context,
                    uri
                )

                val count = 12

                for (
                    index in 0 until count
                ) {

                    if (
                        generation !=
                        thumbnailGeneration
                    ) {
                        break
                    }

                    val fraction =
                        if (count == 1) {
                            0f
                        } else {
                            index.toFloat() /
                                (count - 1).toFloat()
                        }

                    val timeMs =
                        (
                            durationMs *
                                fraction
                        ).toLong()
                            .coerceAtMost(
                                max(
                                    durationMs - 1,
                                    0
                                ).toLong()
                            )

                    val frame =
                        retriever.getFrameAtTime(
                            timeMs * 1000L,
                            MediaMetadataRetriever
                                .OPTION_CLOSEST_SYNC
                        )

                    if (frame != null) {

                        val thumbnail =
                            createThumbnail(
                                frame,
                                120,
                                60
                            )

                        loaded.add(
                            thumbnail
                        )
                    }
                }

            } catch (_: Exception) {

                loaded.forEach {

                    if (!it.isRecycled) {
                        it.recycle()
                    }
                }

                loaded.clear()

            } finally {

                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }

            post {

                if (
                    generation !=
                    thumbnailGeneration
                ) {

                    loaded.forEach {

                        if (!it.isRecycled) {
                            it.recycle()
                        }
                    }

                    return@post
                }

                clearThumbnails()

                thumbnails.addAll(
                    loaded
                )

                invalidate()
            }

        }.start()
    }

    private fun createThumbnail(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {

        val scale =
            max(
                targetWidth.toFloat() /
                    source.width.toFloat(),

                targetHeight.toFloat() /
                    source.height.toFloat()
            )

        val scaledWidth =
            (
                source.width *
                    scale
            ).roundToInt()

        val scaledHeight =
            (
                source.height *
                    scale
            ).roundToInt()

        val scaled =
            Bitmap.createScaledBitmap(
                source,
                scaledWidth,
                scaledHeight,
                true
            )

        if (scaled !== source) {
            source.recycle()
        }

        val left =
            max(
                (scaledWidth -
                    targetWidth) / 2,
                0
            )

        val top =
            max(
                (scaledHeight -
                    targetHeight) / 2,
                0
            )

        val cropWidth =
            minOf(
                targetWidth,
                scaledWidth
            )

        val cropHeight =
            minOf(
                targetHeight,
                scaledHeight
            )

        val result =
            Bitmap.createBitmap(
                scaled,
                left,
                top,
                cropWidth,
                cropHeight
            )

        if (result !== scaled) {
            scaled.recycle()
        }

        return result
    }

    private fun clearThumbnails() {

        thumbnails.forEach {

            if (!it.isRecycled) {
                it.recycle()
            }
        }

        thumbnails.clear()
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {

                val seconds =
                    (
                        event.x - 20f
                    ) / pixelsPerSecond

                val newPosition =
                    (
                        seconds * 1000f
                    ).toInt()

                val safePosition =
                    newPosition.coerceIn(
                        0,
                        durationMs
                    )

                positionMs =
                    safePosition

                onPositionChanged?.invoke(
                    safePosition
                )

                invalidate()

                return true
            }
        }

        return true
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
}
