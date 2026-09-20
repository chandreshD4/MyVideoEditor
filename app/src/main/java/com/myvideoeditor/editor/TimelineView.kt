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
import kotlin.math.min

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val RULER_HEIGHT = 34
        private const val VIDEO_TRACK_HEIGHT = 82
        private const val TEXT_TRACK_HEIGHT = 44
        private const val AUDIO_TRACK_HEIGHT = 58

        private const val CORNER_RADIUS = 8f
        private const val PLAYHEAD_WIDTH = 2f

        private const val MAX_THUMBNAILS = 120
    }

    private var videoUri: Uri? = null

    private var durationMs: Int = 0
    private var positionMs: Int = 0

    private var zoom: Float =
        TimelineMetrics.DEFAULT_ZOOM

    private var thumbnails =
        mutableListOf<Bitmap>()

    private var thumbnailTimes =
        mutableListOf<Long>()

    private var thumbnailGeneration = 0

    private var isDraggingPlayhead = false

    private var onPositionChangedListener:
        ((Int) -> Unit)? = null

    private val rulerPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 190, 205)
            strokeWidth = 1f
        }

    private val rulerTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 224, 235)
            textSize = 11f
            isAntiAlias = true
        }

    private val trackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 24, 38)
            isAntiAlias = true
        }

    private val videoBorderPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 76, 96)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

    private val textTrackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(38, 43, 62)
            isAntiAlias = true
        }

    private val audioTrackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(29, 35, 52)
            isAntiAlias = true
        }

    private val playheadPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = PLAYHEAD_WIDTH
            isAntiAlias = true
        }

    private val playheadHandlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            isAntiAlias = true
        }

    private val waveformPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(110, 170, 255)
            strokeWidth = 2f
            isAntiAlias = true
        }

    private val thumbnailDestination =
        RectF()

    init {
        isFocusable = true
        setBackgroundColor(
            Color.rgb(8, 11, 22)
        )
    }

    fun setVideoUri(uri: Uri?) {
        videoUri = uri
        clearThumbnails()

        if (uri != null && durationMs > 0) {
            loadThumbnails()
        }

        invalidate()
    }

    fun setDuration(duration: Int) {
        durationMs =
            duration.coerceAtLeast(0)

        positionMs =
            TimelineMetrics.clampPosition(
                positionMs,
                durationMs
            )

        clearThumbnails()

        if (videoUri != null && durationMs > 0) {
            loadThumbnails()
        }

        requestLayout()
        invalidate()
    }

    fun setPosition(position: Int) {
        positionMs =
            TimelineMetrics.clampPosition(
                position,
                durationMs
            )

        invalidate()
    }

    fun getPosition(): Int {
        return positionMs
    }

    fun getDuration(): Int {
        return durationMs
    }

    fun setZoom(value: Float) {
        zoom =
            TimelineMetrics.clampZoom(value)

        requestLayout()
        invalidate()
    }

    fun getZoom(): Float {
        return zoom
    }

    fun zoomIn() {
        setZoom(
            TimelineMetrics.zoomIn(zoom)
        )
    }

    fun zoomOut() {
        setZoom(
            TimelineMetrics.zoomOut(zoom)
        )
    }

    fun setOnPositionChangedListener(
        listener: ((Int) -> Unit)?
    ) {
        onPositionChangedListener = listener
    }

    fun clearThumbnails() {
        thumbnailGeneration++

        thumbnails.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }

        thumbnails.clear()
        thumbnailTimes.clear()
    }
    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val contentWidth =
            TimelineMetrics.contentWidth(
                durationMs,
                zoom
            ) +
            TimelineMetrics.timelinePadding(
                zoom
            ) * 2

        val desiredWidth =
            max(
                320,
                contentWidth
            )

        val desiredHeight =
            RULER_HEIGHT +
            VIDEO_TRACK_HEIGHT +
            TEXT_TRACK_HEIGHT +
            AUDIO_TRACK_HEIGHT

        val measuredWidth =
            resolveSize(
                desiredWidth,
                widthMeasureSpec
            )

        val measuredHeight =
            resolveSize(
                desiredHeight,
                heightMeasureSpec
            )

        setMeasuredDimension(
            measuredWidth,
            measuredHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding =
            TimelineMetrics.timelinePadding(
                zoom
            )

        canvas.save()
        canvas.translate(
            padding.toFloat(),
            0f
        )

        drawRuler(canvas)
        drawVideoTrack(canvas)
        drawTextTrack(canvas)
        drawAudioTrack(canvas)
        drawPlayhead(canvas)

        canvas.restore()
    }

    private fun drawRuler(
        canvas: Canvas
    ) {
        val width =
            TimelineMetrics.contentWidth(
                durationMs,
                zoom
            )

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            RULER_HEIGHT.toFloat(),
            trackPaint
        )

        val totalSeconds =
            TimelineMetrics.durationSeconds(
                durationMs
            )

        for (second in 0..totalSeconds) {

            val x =
                TimelineMetrics.secondToX(
                    second.toFloat(),
                    zoom
                )

            if (
                TimelineMetrics.isMajorSecond(
                    second,
                    zoom
                )
            ) {

                canvas.drawLine(
                    x,
                    17f,
                    x,
                    RULER_HEIGHT.toFloat(),
                    rulerPaint
                )

                val label =
                    TimelineMetrics.formatTime(
                        second * 1000L
                    )

                canvas.drawText(
                    label,
                    x + 4f,
                    13f,
                    rulerTextPaint
                )

            } else if (
                TimelineMetrics.isMinorSecond(
                    second,
                    zoom
                )
            ) {

                canvas.drawLine(
                    x,
                    23f,
                    x,
                    RULER_HEIGHT.toFloat(),
                    rulerPaint
                )
            }
        }
    }

    private fun drawVideoTrack(
        canvas: Canvas
    ) {
        val top =
            RULER_HEIGHT.toFloat()

        val bottom =
            top +
            VIDEO_TRACK_HEIGHT

        val width =
            TimelineMetrics.contentWidth(
                durationMs,
                zoom
            )

        canvas.drawRoundRect(
            0f,
            top,
            width.toFloat(),
            bottom,
            CORNER_RADIUS,
            CORNER_RADIUS,
            trackPaint
        )

        drawThumbnails(
            canvas,
            top,
            bottom
        )

        canvas.drawRoundRect(
            0f,
            top,
            width.toFloat(),
            bottom,
            CORNER_RADIUS,
            CORNER_RADIUS,
            videoBorderPaint
        )
    }

    private fun drawTextTrack(
        canvas: Canvas
    ) {
        val top =
            (
                RULER_HEIGHT +
                VIDEO_TRACK_HEIGHT
            ).toFloat()

        val bottom =
            top +
            TEXT_TRACK_HEIGHT

        val width =
            TimelineMetrics.contentWidth(
                durationMs,
                zoom
            )

        canvas.drawRoundRect(
            0f,
            top,
            width.toFloat(),
            bottom,
            CORNER_RADIUS,
            CORNER_RADIUS,
            textTrackPaint
        )
    }

    private fun drawAudioTrack(
        canvas: Canvas
    ) {
        val top =
            (
                RULER_HEIGHT +
                VIDEO_TRACK_HEIGHT +
                TEXT_TRACK_HEIGHT
            ).toFloat()

        val bottom =
            top +
            AUDIO_TRACK_HEIGHT

        val width =
            TimelineMetrics.contentWidth(
                durationMs,
                zoom
            )

        canvas.drawRoundRect(
            0f,
            top,
            width.toFloat(),
            bottom,
            CORNER_RADIUS,
            CORNER_RADIUS,
            audioTrackPaint
        )

        drawPlaceholderWaveform(
            canvas,
            top,
            bottom,
            width
        )
    }
    private fun drawThumbnails(
        canvas: Canvas,
        top: Float,
        bottom: Float
    ) {
        if (thumbnails.isEmpty()) {
            return
        }

        val trackHeight =
            bottom - top

        val thumbnailWidth =
            TimelineMetrics.thumbnailWidth(
                zoom
            ).toFloat()

        for (index in thumbnails.indices) {

            val bitmap =
                thumbnails[index]

            if (bitmap.isRecycled) {
                continue
            }

            val timeMs =
                thumbnailTimes.getOrNull(index)
                    ?: continue

            val x =
                TimelineMetrics.secondToX(
                    timeMs / 1000f,
                    zoom
                )

            thumbnailDestination.set(
                x,
                top,
                x + thumbnailWidth,
                bottom
            )

            canvas.drawBitmap(
                bitmap,
                null,
                thumbnailDestination,
                null
            )
        }
    }

    private fun drawPlaceholderWaveform(
        canvas: Canvas,
        top: Float,
        bottom: Float,
        width: Int
    ) {
        if (width <= 0) {
            return
        }

        val centerY =
            (top + bottom) / 2f

        val amplitude =
            (bottom - top) * 0.32f

        val step =
            12f

        var x = 0f

        while (x < width) {

            val normalized =
                kotlin.math.sin(
                    x * 0.075f
                ).toFloat()

            val height =
                amplitude *
                (0.25f +
                    kotlin.math.abs(
                        normalized
                    ) * 0.75f)

            canvas.drawLine(
                x,
                centerY - height,
                x,
                centerY + height,
                waveformPaint
            )

            x += step
        }
    }

    private fun drawPlayhead(
        canvas: Canvas
    ) {
        val x =
            TimelineMetrics.playheadX(
                positionMs,
                zoom
            )

        val totalHeight =
            RULER_HEIGHT +
            VIDEO_TRACK_HEIGHT +
            TEXT_TRACK_HEIGHT +
            AUDIO_TRACK_HEIGHT

        canvas.drawLine(
            x,
            0f,
            x,
            totalHeight.toFloat(),
            playheadPaint
        )

        canvas.drawCircle(
            x,
            6f,
            6f,
            playheadHandlePaint
        )
    }

    private fun loadThumbnails() {

        val uri =
            videoUri ?: return

        val duration =
            durationMs

        if (duration <= 0) {
            return
        }

        val generation =
            thumbnailGeneration + 1

        thumbnailGeneration =
            generation

        val count =
            TimelineMetrics.thumbnailCount(
                duration,
                zoom
            ).coerceAtMost(
                MAX_THUMBNAILS
            )

        Thread {

            val retriever =
                MediaMetadataRetriever()

            try {

                retriever.setDataSource(
                    context,
                    uri
                )

                val localBitmaps =
                    mutableListOf<Bitmap>()

                val localTimes =
                    mutableListOf<Long>()

                for (index in 0 until count) {

                    if (
                        generation !=
                        thumbnailGeneration
                    ) {
                        break
                    }

                    val timeMs =
                        TimelineMetrics.thumbnailTimeMs(
                            index,
                            duration,
                            zoom
                        )

                    val bitmap =
                        retriever.getFrameAtTime(
                            timeMs * 1000L,
                            MediaMetadataRetriever
                                .OPTION_CLOSEST_SYNC
                        )

                    if (bitmap != null) {

                        val scaled =
                            createThumbnail(
                                bitmap
                            )

                        if (
                            !bitmap.isRecycled &&
                            bitmap !== scaled
                        ) {
                            bitmap.recycle()
                        }

                        localBitmaps.add(
                            scaled
                        )

                        localTimes.add(
                            timeMs
                        )
                    }
                }

                retriever.release()

                post {

                    if (
                        generation !=
                        thumbnailGeneration
                    ) {
                        localBitmaps.forEach {
                            if (!it.isRecycled) {
                                it.recycle()
                            }
                        }
                        return@post
                    }

                    thumbnails.clear()
                    thumbnailTimes.clear()

                    thumbnails.addAll(
                        localBitmaps
                    )

                    thumbnailTimes.addAll(
                        localTimes
                    )

                    invalidate()
                }

            } catch (_: Exception) {

                try {
                    retriever.release()
                } catch (_: Exception) {
                }

                post {
                    invalidate()
                }
            }

        }.start()
    }

    private fun createThumbnail(
        source: Bitmap
    ): Bitmap {

        val targetWidth =
            TimelineMetrics.thumbnailWidth(
                zoom
            )

        val targetHeight =
            VIDEO_TRACK_HEIGHT

        val sourceRatio =
            source.width.toFloat() /
                    source.height.toFloat()

        val targetRatio =
            targetWidth.toFloat() /
                    targetHeight.toFloat()

        val cropWidth: Int
        val cropHeight: Int

        if (sourceRatio > targetRatio) {

            cropHeight =
                source.height

            cropWidth =
                (
                    source.height *
                    targetRatio
                ).toInt()
                    .coerceAtMost(
                        source.width
                    )

        } else {

            cropWidth =
                source.width

            cropHeight =
                (
                    source.width /
                    targetRatio
                ).toInt()
                    .coerceAtMost(
                        source.height
                    )
        }

        val left =
            (source.width - cropWidth) / 2

        val top =
            (source.height - cropHeight) / 2

        return Bitmap.createBitmap(
            source,
            left,
            top,
            cropWidth,
            cropHeight
        )
    }
    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                isDraggingPlayhead = true

                updatePositionFromTouch(
                    event.x
                )

                parent.requestDisallowInterceptTouchEvent(
                    true
                )

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                if (isDraggingPlayhead) {

                    updatePositionFromTouch(
                        event.x
                    )
                }

                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                isDraggingPlayhead = false

                parent.requestDisallowInterceptTouchEvent(
                    false
                )

                return true
            }
        }

        return true
    }

    private fun updatePositionFromTouch(
        touchX: Float
    ) {
        val padding =
            TimelineMetrics.timelinePadding(
                zoom
            )

        val timelineX =
            (
                touchX - padding
            ).coerceAtLeast(0f)

        val position =
            TimelineMetrics.positionFromX(
                timelineX,
                zoom
            )

        val clamped =
            TimelineMetrics.clampPosition(
                position,
                durationMs
            )

        positionMs =
            clamped

        onPositionChangedListener?.invoke(
            positionMs
        )

        invalidate()
    }

    override fun onDetachedFromWindow() {
        clearThumbnails()
        super.onDetachedFromWindow()
    }
}
