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
        const val EXTRA_MEDIA_URI = "media_uri"
        const val EXTRA_PROJECT_NAME = "project_name"

        const val REQUEST_MEDIA = 301
    }

    private var aspectRatio = "9:16"
    private var sourceType = "blank"
    private var mediaUri: String? = null
    private var projectName = "New Project"

    private var selectedColor = Color.BLACK
    private var selectedColorName = "Black"

    private lateinit var importedMedia: TextView
    private lateinit var backgroundSelected: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_media_source)

        aspectRatio =
            intent.getStringExtra(
                FormatActivity.EXTRA_ASPECT_RATIO
            ) ?: "9:16"

        projectName =
            intent.getStringExtra(
                FormatActivity.EXTRA_PROJECT_NAME
            ) ?: "New Project"

        sourceType =
            intent.getStringExtra(
                EXTRA_SOURCE_TYPE
            ) ?: "blank"

        mediaUri =
            intent.getStringExtra(
                EXTRA_MEDIA_URI
            )

        importedMedia =
            findViewById(R.id.importedMedia)

        backgroundSelected =
            findViewById(R.id.backgroundSelected)

        setupClicks()
        updateImportedMedia()
        updateSelectedColor()
    }

    private fun setupClicks() {

        findViewById<TextView>(R.id.mediaBack)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.mediaClose)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.importButton)
            .setOnClickListener {
                openMediaPicker()
            }

        findViewById<TextView>(R.id.mediaDevice)
            .setOnClickListener {
                openMediaPicker()
            }

        findViewById<TextView>(R.id.mediaPhotos)
            .setOnClickListener {
                openImagePicker()
            }

        findViewById<TextView>(R.id.backgroundBlack)
            .setOnClickListener {
                selectColor(
                    "Black",
                    Color.BLACK
                )
            }

        findViewById<TextView>(R.id.backgroundWhite)
            .setOnClickListener {
                selectColor(
                    "White",
                    Color.WHITE
                )
            }

        findViewById<TextView>(R.id.backgroundRed)
            .setOnClickListener {
                selectColor(
                    "Red",
                    Color.rgb(255, 77, 90)
                )
            }

        findViewById<TextView>(R.id.backgroundGreen)
            .setOnClickListener {
                selectColor(
                    "Green",
                    Color.rgb(32, 183, 122)
                )
            }

        findViewById<TextView>(R.id.backgroundBlue)
            .setOnClickListener {
                selectColor(
                    "Blue",
                    Color.rgb(62, 139, 255)
                )
            }

        findViewById<TextView>(R.id.backgroundYellow)
            .setOnClickListener {
                selectColor(
                    "Yellow",
                    Color.rgb(255, 216, 61)
                )
            }

        findViewById<TextView>(R.id.backgroundDone)
            .setOnClickListener {
                continueToEditor()
            }
    }

    private fun selectColor(
        name: String,
        color: Int
    ) {
        selectedColorName = name
        selectedColor = color
        updateSelectedColor()
    }

    private fun updateSelectedColor() {

        backgroundSelected.text =
            "Selected: $selectedColorName"

        val colors = listOf(
            Pair(
                R.id.backgroundBlack,
                "Black"
            ),
            Pair(
                R.id.backgroundWhite,
                "White"
            ),
            Pair(
                R.id.backgroundRed,
                "Red"
            ),
            Pair(
                R.id.backgroundGreen,
                "Green"
            ),
            Pair(
                R.id.backgroundBlue,
                "Blue"
            ),
            Pair(
                R.id.backgroundYellow,
                "Yellow"
            )
        )

        for ((id, name) in colors) {

            val view =
                findViewById<TextView>(id)

            view.alpha =
                if (name == selectedColorName) {
                    1.0f
                } else {
                    0.72f
                }

            view.scaleX =
                if (name == selectedColorName) {
                    1.03f
                } else {
                    1.0f
                }

            view.scaleY =
                if (name == selectedColorName) {
                    1.03f
                } else {
                    1.0f
                }
        }
    }

    private fun updateImportedMedia() {

        if (mediaUri.isNullOrEmpty()) {

            importedMedia.text =
                "＋  Import photo or video"

            sourceType = "blank"

        } else {

            importedMedia.text =
                if (sourceType == "video") {
                    "✓  Video imported"
                } else {
                    "✓  Photo imported"
                }
        }
    }

    private fun openMediaPicker() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

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
            REQUEST_MEDIA
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
            REQUEST_MEDIA
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
            requestCode != REQUEST_MEDIA ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri =
            data?.data ?: return

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        mediaUri = uri.toString()

        val mime =
            contentResolver.getType(uri)
                ?: ""

        sourceType =
            if (mime.startsWith("video/")) {
                "video"
            } else {
                "image"
            }

        updateImportedMedia()
    }

    private fun continueToEditor() {

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
                    selectedColor
                )

                putExtra(
                    EditorActivity.EXTRA_PROJECT_NAME,
                    projectName
                )

                if (!mediaUri.isNullOrEmpty()) {
                    putExtra(
                        EditorActivity.EXTRA_VIDEO_URI,
                        mediaUri
                    )
                }
            }

        startActivity(intent)
        finish()
    }
}
