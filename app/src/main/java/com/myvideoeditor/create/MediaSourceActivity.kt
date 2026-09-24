package com.myvideoeditor.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        const val REQUEST_COLOR = 302
        const val REQUEST_AUDIO = 303
        const val REQUEST_FOLDER = 304
        const val REQUEST_PERMISSION = 305
    }

    private var aspectRatio = "9:16"
    private var sourceType = "blank"
    private var mediaUri: String? = null
    private var projectName = "New Project"

    private var selectedColor = Color.BLACK
    private var selectedColorName = "Black"

    private lateinit var importedMedia: TextView
    private lateinit var colorsBox: TextView
    private lateinit var mediaEmptyText: TextView
    private lateinit var mediaRecycler: RecyclerView

    private val folders = ArrayList<MediaFolder>()

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

        importedMedia =
            findViewById(R.id.importedMedia)

        colorsBox =
            findViewById(R.id.colorsBox)

        mediaEmptyText =
            findViewById(R.id.mediaEmptyText)

        mediaRecycler =
            findViewById(R.id.mediaFoldersRecyclerView)

        setupRecycler()
        setupClicks()
        updateSelectedColor()
        updateImportedMedia()

        requestMediaPermission()
    }

    private fun setupRecycler() {

        mediaRecycler.layoutManager =
            GridLayoutManager(
                this,
                2
            )

        mediaRecycler.isNestedScrollingEnabled =
            false

        mediaRecycler.adapter =
            MediaFolderAdapter(
                folders
            ) { folder ->

                openFolderMedia(folder)
            }
    }

    private fun setupClicks() {

        findViewById<TextView>(
            R.id.mediaBack
        ).setOnClickListener {
            finish()
        }

        findViewById<TextView>(
            R.id.mediaClose
        ).setOnClickListener {
            finish()
        }

        // 1. MEDIA
        findViewById<TextView>(
            R.id.importButton
        ).setOnClickListener {
            openMediaPicker()
        }

        // 2. COLORS
        colorsBox.setOnClickListener {
            openColorsScreen()
        }

        // 3. DEVICE FILES
        findViewById<TextView>(
            R.id.mediaDevice
        ).setOnClickListener {
            openAllFiles()
        }

        // 4. PHOTOS
        findViewById<TextView>(
            R.id.mediaPhotos
        ).setOnClickListener {
            openImagePicker()
        }

        // 5. AUDIO
        findViewById<TextView>(
            R.id.boxFive
        ).setOnClickListener {
            openAudioPicker()
        }

        // 6. MORE
        findViewById<TextView>(
            R.id.boxSix
        ).setOnClickListener {
            openFolderPicker()
        }

        // CONTINUE
        findViewById<TextView>(
            R.id.backgroundDone
        ).setOnClickListener {
            continueToEditor()
        }
    }

    private fun requestMediaPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

            if (
                checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    permissions,
                    REQUEST_PERMISSION
                )

            } else {

                loadMediaFolders()
            }

        } else {

            if (
                checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_PERMISSION
                )

            } else {

                loadMediaFolders()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == REQUEST_PERMISSION
        ) {

            loadMediaFolders()
        }
    }

    private fun loadMediaFolders() {

        mediaEmptyText.text =
            "Scanning your photos and videos..."

        Thread {

            val result =
                scanMediaFolders()

            runOnUiThread {

                folders.clear()
                folders.addAll(result)

                mediaRecycler.adapter?.notifyDataSetChanged()

                if (folders.isEmpty()) {

                    mediaEmptyText.visibility =
                        TextView.VISIBLE

                    mediaEmptyText.text =
                        "No photos or videos found"

                } else {

                    mediaEmptyText.visibility =
                        TextView.GONE
                }
            }

        }.start()
    }

    private fun scanMediaFolders(): ArrayList<MediaFolder> {

        val result =
            ArrayList<MediaFolder>()

        val seen =
            HashSet<String>()

        val projection =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MEDIA_TYPE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATE_ADDED
            )

        val uri =
            MediaStore.Files.getContentUri(
                "external"
            )

        try {

            contentResolver.query(
                uri,
                projection,
                "${MediaStore.MediaColumns.MEDIA_TYPE}=? OR ${MediaStore.MediaColumns.MEDIA_TYPE}=?",
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                ),
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->

                val idIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns._ID
                    )

                val typeIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.MEDIA_TYPE
                    )

                val bucketIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
                    )

                val pathIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.RELATIVE_PATH
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(idIndex)

                    val mediaType =
                        cursor.getInt(typeIndex)

                    val bucket =
                        if (bucketIndex >= 0)
                            cursor.getString(bucketIndex)
                        else
                            null

                    val relativePath =
                        if (pathIndex >= 0)
                            cursor.getString(pathIndex)
                        else
                            null

                    val folderName =
                        getFolderName(
                            bucket,
                            relativePath
                        )

                    val key =
                        folderName.lowercase()

                    if (seen.contains(key)) {
                        continue
                    }

                    val mediaUri =
                        if (
                            mediaType ==
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        ) {

                            Uri.withAppendedPath(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id.toString()
                            )

                        } else {

                            Uri.withAppendedPath(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id.toString()
                            )
                        }

                    result.add(
                        MediaFolder(
                            folderName,
                            mediaUri,
                            mediaType
                        )
                    )

                    seen.add(key)
                }
            }

        } catch (
            _: Exception
        ) {
        }

        return result
    }

    private fun getFolderName(
        bucket: String?,
        relativePath: String?
    ): String {

        if (!bucket.isNullOrBlank()) {
            return bucket
        }

        if (!relativePath.isNullOrBlank()) {

            val clean =
                relativePath
                    .trimEnd('/')

            val index =
                clean.lastIndexOf('/')

            if (index >= 0) {
                return clean.substring(
                    index + 1
                )
            }

            return clean
        }

        return "Other Media"
    }

    private fun openFolderMedia(
        folder: MediaFolder
    ) {

        mediaUri =
            folder.uri.toString()

        sourceType =
            if (
                folder.mediaType ==
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            ) {
                "video"
            } else {
                "image"
            }

        updateImportedMedia()
    }

    private fun openColorsScreen() {

        val intent =
            Intent(
                this,
                ColorsActivity::class.java
            )

        startActivityForResult(
            intent,
            REQUEST_COLOR
        )
    }

    private fun updateSelectedColor() {

        colorsBox.text =
            "●\nCOLORS\n$selectedColorName"
    }

    private fun updateImportedMedia() {

        importedMedia.text =
            if (mediaUri.isNullOrEmpty()) {

                "No media selected"

            } else {

                if (sourceType == "video") {
                    "✓ Video selected"
                } else {
                    "✓ Media selected"
                }
            }
    }

    private fun openMediaPicker() {

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
            REQUEST_MEDIA
        )
    }

    private fun openImagePicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

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

    private fun openAudioPicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "audio/*"

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            REQUEST_AUDIO
        )
    }

    private fun openAllFiles() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "*/*"

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

    private fun openFolderPicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            )

        startActivityForResult(
            intent,
            REQUEST_FOLDER
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
            requestCode == REQUEST_COLOR &&
            resultCode == RESULT_OK
        ) {

            selectedColor =
                data?.getIntExtra(
                    ColorsActivity.EXTRA_COLOR,
                    Color.BLACK
                ) ?: Color.BLACK

            selectedColorName =
                data?.getStringExtra(
                    ColorsActivity.EXTRA_COLOR_NAME
                ) ?: "Black"

            updateSelectedColor()

            return
        }

        if (
            requestCode == REQUEST_FOLDER &&
            resultCode == RESULT_OK
        ) {

            val uri =
                data?.data ?: return

            mediaUri =
                uri.toString()

            sourceType =
                "folder"

            importedMedia.text =
                "✓ Folder selected"

            return
        }

        if (
            requestCode != REQUEST_MEDIA &&
            requestCode != REQUEST_AUDIO
        ) {
            return
        }

        if (
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri =
            data?.data ?: return

        try {

            contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

        } catch (
            _: SecurityException
        ) {
        }

        mediaUri =
            uri.toString()

        if (
            requestCode == REQUEST_AUDIO
        ) {

            sourceType =
                "audio"

            importedMedia.text =
                "✓ Audio selected"

            return
        }

        val mime =
            contentResolver.getType(uri)
                ?: ""

        sourceType =
            if (
                mime.startsWith("video/")
            ) {
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

                if (
                    !mediaUri.isNullOrEmpty()
                ) {

                    putExtra(
                        EditorActivity.EXTRA_VIDEO_URI,
                        mediaUri
                    )
                }
            }

        startActivity(intent)

        finish()
    }

    data class MediaFolder(
        val name: String,
        val uri: Uri,
        val mediaType: Int
    )

    private inner class MediaFolderAdapter(
        private val items: List<MediaFolder>,
        private val onClick:
            (MediaFolder) -> Unit
    ) : RecyclerView.Adapter<MediaFolderAdapter.Holder>() {

        inner class Holder(
            val layout: LinearLayout
        ) : RecyclerView.ViewHolder(layout)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): Holder {

            val card =
                LinearLayout(parent.context).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    gravity =
                        Gravity.CENTER

                    setPadding(
                        dp(6),
                        dp(6),
                        dp(6),
                        dp(6)
                    )

                    setBackgroundColor(
                        Color.rgb(
                            18,
                            23,
                            31
                        )
                    )
                }

            val image =
                ImageView(parent.context).apply {

                    id =
                        android.R.id.icon

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP
                }

            card.addView(
                image,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(125)
                )
            )

            val title =
                TextView(parent.context).apply {

                    id =
                        android.R.id.text1

                    gravity =
                        Gravity.CENTER

                    textColor =
                        Color.WHITE

                    textSize =
                        13f

                    setPadding(
                        dp(3),
                        dp(6),
                        dp(3),
                        dp(3)
                    )
                }

            card.addView(
                title,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(42)
                )
            )

            val params =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(175)
                )

            params.setMargins(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
            )

            card.layoutParams =
                params

            return Holder(card)
        }

        override fun onBindViewHolder(
            holder: Holder,
            position: Int
        ) {

            val item =
                items[position]

            val image =
                holder.layout.findViewById<ImageView>(
                    android.R.id.icon
                )

            val title =
                holder.layout.findViewById<TextView>(
                    android.R.id.text1
                )

            title.text =
                item.name

            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )

            Thread {

                try {

                    val bitmap =
                        contentResolver.loadThumbnail(
                            item.uri,
                            Size(
                                dp(300),
                                dp(200)
                            ),
                            null
                        )

                    runOnUiThread {

                        if (
                            holder.bindingAdapterPosition ==
                            position
                        ) {

                            image.setImageBitmap(
                                bitmap
                            )
                        }
                    }

                } catch (
                    _: Exception
                ) {
                }

            }.start()

            holder.layout.setOnClickListener {
                onClick(item)
            }
        }

        override fun getItemCount(): Int {
            return items.size
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
