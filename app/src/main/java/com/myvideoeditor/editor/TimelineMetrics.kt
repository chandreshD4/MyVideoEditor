package com.myvideoeditor.editor

object TimelineMetrics {

    const val MIN_ZOOM = 0.25f
    const val MAX_ZOOM = 8.0f
    const val DEFAULT_ZOOM = 1.0f

    /*
     * 100% zoom पर एक सेकंड की video को 80 pixels
     * जगह मिलेगी।
     *
     * इससे छोटी video में हर second के frames साफ
     * दिखाई देंगे और बड़ी video HorizontalScrollView
     * से आराम से चल सकेगी।
     */
    const val BASE_PIXELS_PER_SECOND = 80f

    fun clampZoom(
        zoom: Float
    ): Float {

        return zoom.coerceIn(
            MIN_ZOOM,
            MAX_ZOOM
        )
    }

    fun pixelsPerSecond(
        zoom: Float
    ): Float {

        return BASE_PIXELS_PER_SECOND *
                clampZoom(zoom)
    }

    fun pixelsForDuration(
        durationMs: Int,
        zoom: Float
    ): Int {

        if (durationMs <= 0) {
            return 320
        }

        val seconds =
            durationMs / 1000f

        return (
            seconds *
            pixelsPerSecond(zoom)
        ).toInt()
            .coerceAtLeast(320)
    }

    fun zoomIn(
        currentZoom: Float
    ): Float {

        return clampZoom(
            currentZoom * 1.25f
        )
    }

    fun zoomOut(
        currentZoom: Float
    ): Float {

        return clampZoom(
            currentZoom / 1.25f
        )
    }

    fun chooseMajorStepSeconds(
        zoom: Float
    ): Int {

        val value =
            clampZoom(zoom)

        return when {
            value >= 4.0f -> 1
            value >= 2.0f -> 2
            value >= 1.0f -> 5
            value >= 0.5f -> 10
            else -> 30
        }
    }

    fun chooseMinorStepSeconds(
        zoom: Float
    ): Int {

        val value =
            clampZoom(zoom)

        return when {
            value >= 2.0f -> 1
            value >= 1.0f -> 1
            value >= 0.5f -> 2
            else -> 5
        }
    }

    fun formatTime(
        milliseconds: Long
    ): String {

        val totalSeconds =
            (milliseconds / 1000L)
                .coerceAtLeast(0L)

        val hours =
            totalSeconds / 3600L

        val minutes =
            (totalSeconds % 3600L) / 60L

        val seconds =
            totalSeconds % 60L

        return if (hours > 0L) {

            String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        } else {

            String.format(
                "%02d:%02d",
                minutes,
                seconds
            )
        }
    }

    /*
     * अगले भाग में Timeline की exact pixel position,
     * second → X और X → second conversion तथा
     * thumbnail spacing की गणना आएगी।
     */

    fun secondToX(
        second: Float,
        zoom: Float
    ): Float {
        return second *
                pixelsPerSecond(zoom)
    }

    fun xToSecond(
        x: Float,
        zoom: Float
    ): Float {

        val pixels =
            pixelsPerSecond(zoom)

        if (pixels <= 0f) {
            return 0f
        }

        return (
            x / pixels
        ).coerceAtLeast(0f)
    }

    fun thumbnailCount(
        durationMs: Int,
        zoom: Float
    ): Int {

        if (durationMs <= 0) {
            return 1
        }

        val durationSeconds =
            durationMs / 1000f

        val spacing =
            pixelsPerSecond(zoom)

        return (
            durationSeconds *
            spacing /
            80f
        ).toInt()
            .coerceIn(1, 120)
    }

    fun thumbnailWidth(
        zoom: Float
    ): Int {

        return (
            pixelsPerSecond(zoom)
        ).toInt()
            .coerceIn(40, 320)
    }

    fun rulerStepPixels(
        zoom: Float,
        seconds: Int
    ): Float {

        return secondToX(
            seconds.toFloat(),
            zoom
        )
    }

    fun isMajorSecond(
        second: Int,
        zoom: Float
    ): Boolean {

        val step =
            chooseMajorStepSeconds(zoom)

        return second % step == 0
    }

    fun isMinorSecond(
        second: Int,
        zoom: Float
    ): Boolean {

        val major =
            chooseMajorStepSeconds(zoom)

        val minor =
            chooseMinorStepSeconds(zoom)

        if (minor <= 0) {
            return false
        }

        if (second % minor != 0) {
            return false
        }

        return second % major != 0
    }

    fun durationSeconds(
        durationMs: Int
    ): Int {

        if (durationMs <= 0) {
            return 0
        }

        return (
            durationMs / 1000f
        ).let { seconds ->

            kotlin.math.ceil(
                seconds.toDouble()
            ).toInt()
        }
    }

    fun contentWidth(
        durationMs: Int,
        zoom: Float
    ): Int {

        val duration =
            durationSeconds(
                durationMs
            )

        val width =
            secondToX(
                duration.toFloat(),
                zoom
            )

        return width
            .toInt()
            .coerceAtLeast(320)
    }

    /*
     * Timeline के दोनों सिरों पर थोड़ी खाली जगह।
     * इससे पहला और आखिरी frame बिल्कुल किनारे
     * से चिपका हुआ नहीं दिखाई देगा।
     */

    fun timelinePadding(
        zoom: Float
    ): Int {

        return (
            pixelsPerSecond(zoom) * 0.5f
        ).toInt()
            .coerceIn(40, 160)
    }

    /*
     * अगला भाग यहाँ से जारी होगा।
     * इसमें Timeline ruler के visible range,
     * zoom level और playback position की
     * calculation आएगी।
     */
    fun visibleSecondsForWidth(
        viewWidth: Int,
        zoom: Float
    ): Float {

        if (viewWidth <= 0) {
            return 0f
        }

        val pixels =
            pixelsPerSecond(zoom)

        if (pixels <= 0f) {
            return 0f
        }

        return viewWidth / pixels
    }

    fun nearestSecond(
        second: Float
    ): Int {

        return kotlin.math.round(
            second.coerceAtLeast(0f)
        ).toInt()
    }

    fun snapPositionToSecond(
        positionMs: Int
    ): Int {

        if (positionMs <= 0) {
            return 0
        }

        val second =
            nearestSecond(
                positionMs / 1000f
            )

        return second * 1000
    }

    fun playheadX(
        positionMs: Int,
        zoom: Float
    ): Float {

        if (positionMs <= 0) {
            return 0f
        }

        return secondToX(
            positionMs / 1000f,
            zoom
        )
    }

    fun positionFromX(
        x: Float,
        zoom: Float
    ): Int {

        val second =
            xToSecond(
                x,
                zoom
            )

        return (
            second * 1000f
        ).toInt()
            .coerceAtLeast(0)
    }

    fun thumbnailTimeMs(
        index: Int,
        durationMs: Int,
        zoom: Float
    ): Long {

        if (index <= 0) {
            return 0L
        }

        if (durationMs <= 0) {
            return 0L
        }

        val count =
            thumbnailCount(
                durationMs,
                zoom
            )

        if (count <= 1) {
            return 0L
        }

        val fraction =
            index.toFloat() /
                    (count - 1).toFloat()

        return (
            durationMs *
            fraction
        ).toLong()
            .coerceIn(
                0L,
                durationMs.toLong()
            )
    }

    fun formatZoom(
        zoom: Float
    ): String {

        val value =
            clampZoom(zoom)

        return if (value >= 1f) {
            String.format(
                "%.1fx",
                value
            )
        } else {
            String.format(
                "%.2fx",
                value
            )
        }
    }

    fun zoomPercent(
        zoom: Float
    ): Int {

        return (
            clampZoom(zoom) * 100f
        ).toInt()
    }

    fun canZoomIn(
        zoom: Float
    ): Boolean {

        return clampZoom(zoom) <
                MAX_ZOOM
    }

    fun canZoomOut(
        zoom: Float
    ): Boolean {

        return clampZoom(zoom) >
                MIN_ZOOM
    }

    fun clampPosition(
        positionMs: Int,
        durationMs: Int
    ): Int {

        if (durationMs <= 0) {
            return 0
        }

        return positionMs.coerceIn(
            0,
            durationMs
        )
    }

    fun durationLabel(
        durationMs: Int
    ): String {

        return formatTime(
            durationMs.toLong()
        )
    }
}
