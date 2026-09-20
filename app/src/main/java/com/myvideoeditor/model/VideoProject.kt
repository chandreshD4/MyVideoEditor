package com.myvideoeditor.model

data class VideoProject(
    val id: Long,
    val name: String,
    val videoUri: String,
    val thumbnailUri: String? = null,
    val duration: Long = 0L,
    val lastEdited: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false
)
