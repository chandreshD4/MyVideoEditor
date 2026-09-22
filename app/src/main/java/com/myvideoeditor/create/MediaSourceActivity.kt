package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.myvideoeditor.R
import com.myvideoeditor.editor.EditorActivity

class MediaSourceActivity : Activity() {

    companion object {
        const val EXTRA_ASPECT_RATIO = "aspect_ratio"
        const val EXTRA_SOURCE_TYPE = "source_type"
        const val EXTRA_BACKGROUND_COLOR = "background_color"

        const val REQUEST_VIDEO = 201
        const val REQUEST_IMAGE = 202
    }

    private var aspectRatio = "9:16"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_media_source
        )

        aspectRatio =
            intent.getStringExtra(
                FormatActivity.EXTRA_ASPECT_RATIO
            ) ?: "9:16"

        findViewById<TextView>(R.id.mediaBack)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.mediaClose)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.sourceVideo)
            .setOnClickListener {
                openVideoPicker()
            }

        findViewById<TextView>(R.id.sourcePhoto)
            .setOnClickListener {
                openImagePicker()
            }

        findViewById<TextView>(R.id.mediaDevice)
            .setOnClickListener {
                openVideoPicker()
            }

        findViewById<TextView>(R.id.mediaPhotos)
            .setOnClickListener {
                openImagePicker()
            }

        findViewById<TextView>(R.id.backgroundWhite)
            .setOnClickListener {
                openEditor(
                    sourceType = "blank",
                    backgroundColor = Color.WHITE
                )
            }

        findViewById<TextView>(R.id.backgroundBlack)
            .setOnClickListener {
                openEditor(
                    sourceType = "blank",
                    backgroundColor = Color.BLACK
                )
            }

        findViewById<TextView>(R.id.backgroundGreen)
            .setOnClickListener {
                openEditor(
                    sourceType = "blank",
                    backgroundColor = Color.GREEN
                )
            }

        findViewById<TextView>(R.id.backgroundYellow)
            .setOnClickListener {
                openEditor(
                    sourceType = "blank",
                    backgroundColor = Color.YELLOW
                )
            }
    }

    private fun openVideoPicker() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "video/*"

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            REQUEST_VIDEO
        )
    }

    private fun openImagePicker() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "image/*"

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            REQUEST_IMAGE
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

        if (resultCode != RESULT_OK) {
            return
        }

        val uri = data?.data ?: return

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        when (requestCode) {

            REQUEST_VIDEO -> {
                openEditor(
                    sourceType = "video",
                    mediaUri = uri.toString()
                )
            }

            REQUEST_IMAGE -> {
                openEditor(
                    sourceType = "image",
                    mediaUri = uri.toString()
                )
            }
        }
    }

    private fun openEditor(
        sourceType: String,
        backgroundColor: Int = Color.TRANSPARENT,
        mediaUri: String? = null
    ) {

        val intent =
            Intent(
                this,
                EditorActivity::class.java
            ).apply {

                putExtra(
                    EditorActivity.EXTRA_ASPECT_RATIO,
                    aspectRatio
                )

                putExtra(
                    EditorActivity.EXTRA_SOURCE_TYPE,
                    sourceType
                )

                putExtra(
                    EditorActivity.EXTRA_BACKGROUND_COLOR,
                    backgroundColor
                )

                if (mediaUri != null) {
                    putExtra(
                        EditorActivity.EXTRA_VIDEO_URI,
                        mediaUri
                    )
                }

                putExtra(
                    EditorActivity.EXTRA_PROJECT_NAME,
                    "New Project"
                )
            }

        startActivity(intent)
        finish()
    }
}
