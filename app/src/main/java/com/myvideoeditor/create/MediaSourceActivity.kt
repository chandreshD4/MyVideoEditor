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

        const val EXTRA_ASPECT_RATIO =
            "aspect_ratio"

        const val EXTRA_SOURCE_TYPE =
            "source_type"

        const val EXTRA_BACKGROUND_COLOR =
            "background_color"

        const val EXTRA_MEDIA_URI =
            "media_uri"

        const val EXTRA_PROJECT_NAME =
            "project_name"

        const val REQUEST_MEDIA = 301
        const val REQUEST_COLOR = 302
        const val REQUEST_AUDIO = 303
        const val REQUEST_FOLDER = 304
        const val REQUEST_PERMISSION = 305
        const val REQUEST_FOLDER_MEDIA = 306
    }

    private var aspectRatio = "9:16"

    private var sourceType = "blank"

    private var mediaUri: String? = null

    private var projectName =
        "New Project"

    private var selectedColor =
        Color.BLACK

    private var selectedColorName =
        "Black"

    private lateinit var colorsBox:
        TextView

    private lateinit var mediaEmptyText:
        TextView

    private lateinit var mediaRecycler:
        RecyclerView

    private val folders =
        ArrayList<MediaFolder>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_media_source
        )

        aspectRatio =
            intent.getStringExtra(
                FormatActivity.EXTRA_ASPECT_RATIO
            ) ?: "9:16"

        projectName =
            intent.getStringExtra(
                FormatActivity.EXTRA_PROJECT_NAME
            ) ?: "New Project"

        colorsBox =
            findViewById(
                R.id.colorsBox
            )

        mediaEmptyText =
            findViewById(
                R.id.mediaEmptyText
            )

        mediaRecycler =
            findViewById(
                R.id.mediaFoldersRecyclerView
            )

        setupRecycler()

        setupClicks()

        updateSelectedColor()

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
            )
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

        // MEDIA

        findViewById<TextView>(
            R.id.importButton
        ).setOnClickListener {

            openMediaPicker()
        }

        // COLORS

        colorsBox.setOnClickListener {

            openColorsScreen()
        }

        // DEVICE FILES

        findViewById<TextView>(
            R.id.mediaDevice
        ).setOnClickListener {

            openAllFiles()
        }

        // PHOTOS

        findViewById<TextView>(
            R.id.mediaPhotos
        ).setOnClickListener {

            openImagePicker()
        }

        // AUDIO

        findViewById<TextView>(
            R.id.boxFive
        ).setOnClickListener {

            openAudioPicker()
        }

        // MORE / FOLDER

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

        if (
            Build.VERSION.SDK_INT >= 33
        ) {

            val imagesGranted =
                checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES
                ) ==
                    PackageManager.PERMISSION_GRANTED

            val videosGranted =
                checkSelfPermission(
                    Manifest.permission.READ_MEDIA_VIDEO
                ) ==
                    PackageManager.PERMISSION_GRANTED

            if (
                !imagesGranted ||
                !videosGranted
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ),
                    REQUEST_PERMISSION
                )

            } else {

                loadMediaFolders()
            }

        } else {

            val storageGranted =
                checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ==
                    PackageManager.PERMISSION_GRANTED

            if (
                !storageGranted
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
            requestCode ==
            REQUEST_PERMISSION
        ) {

            loadMediaFolders()
        }
    }

    private fun loadMediaFolders() {

        mediaEmptyText.visibility =
            TextView.VISIBLE

        mediaEmptyText.text =
            "Scanning your media..."

        Thread {

            val result =
                scanMediaFolders()

            runOnUiThread {

                folders.clear()

                folders.addAll(
                    result
                )

                mediaRecycler.adapter
                    ?.notifyDataSetChanged()

                if (
                    folders.isEmpty()
                ) {

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

    private fun scanMediaFolders():
        ArrayList<MediaFolder> {

        val result =
            ArrayList<MediaFolder>()

        val folderKeys =
            HashSet<String>()

        val projection =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.RELATIVE_PATH
            )

        val filesUri =
            MediaStore.Files.getContentUri(
                "external"
            )

        val selection =
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"

        val selectionArgs =
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    .toString(),

                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    .toString()
            )

        try {

            contentResolver.query(
                filesUri,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->

                val idIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns._ID
                    )

                val typeIndex =
                    cursor.getColumnIndex(
                        MediaStore.Files.FileColumns.MEDIA_TYPE
                    )

                val nameIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.DISPLAY_NAME
                    )

                val bucketIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
                    )

                val dateIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.DATE_ADDED
                    )

                val pathIndex =
                    cursor.getColumnIndex(
                        MediaStore.MediaColumns.RELATIVE_PATH
                    )

                while (
                    cursor.moveToNext()
                ) {

                    if (
                        idIndex < 0 ||
                        typeIndex < 0
                    ) {
                        continue
                    }

                    val id =
                        cursor.getLong(
                            idIndex
                        )

                    val mediaType =
                        cursor.getInt(
                            typeIndex
                        )

                    val displayName =
                        if (
                            nameIndex >= 0
                        ) {

                            cursor.getString(
                                nameIndex
                            ) ?: ""

                        } else {

                            ""
                        }

                    val bucket =
                        if (
                            bucketIndex >= 0
                        ) {

                            cursor.getString(
                                bucketIndex
                            )

                        } else {

                            null
                        }

                    val relativePath =
                        if (
                            Build.VERSION.SDK_INT >= 29 &&
                            pathIndex >= 0
                        ) {

                            cursor.getString(
                                pathIndex
                            )

                        } else {

                            null
                        }

                    val dateAdded =
                        if (
                            dateIndex >= 0
                        ) {

                            cursor.getLong(
                                dateIndex
                            )

                        } else {

                            0L
                        }

                    val folderName =
                        getFolderName(
                            bucket,
                            relativePath
                        )

                    val folderKey =
                        getFolderKey(
                            bucket,
                            relativePath
                        )

                    if (
                        folderKeys.contains(
                            folderKey
                        )
                    ) {

                        continue
                    }

                    val itemUri =
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
                            name = folderName,
                            uri = itemUri,
                            mediaType = mediaType,
                            relativePath = relativePath,
                            bucketName = bucket,
                            latestFileName = displayName,
                            latestDate = dateAdded
                        )
                    )

                    folderKeys.add(
                        folderKey
                    )
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

        if (
            !bucket.isNullOrBlank()
        ) {

            return bucket
        }

        if (
            !relativePath.isNullOrBlank()
        ) {

            val clean =
                relativePath.trimEnd(
                    '/'
                )

            val index =
                clean.lastIndexOf(
                    '/'
                )

            if (
                index >= 0
            ) {

                return clean.substring(
                    index + 1
                )
            }

            return clean
        }

        return "Other Media"
    }

    private fun getFolderKey(
        bucket: String?,
        relativePath: String?
    ): String {

        if (
            !relativePath.isNullOrBlank()
        ) {

            return relativePath
                .trim()
                .lowercase()
        }

        if (
            !bucket.isNullOrBlank()
        ) {

            return bucket
                .trim()
                .lowercase()
        }

        return "other_media"
    }

    private fun openFolder(
        folder: MediaFolder
    ) {

        val intent =
            Intent(
                this,
                FolderMediaActivity::class.java
            ).apply {

                putExtra(
                    FolderMediaActivity.EXTRA_FOLDER_NAME,
                    folder.name
                )

                putExtra(
                    FolderMediaActivity.EXTRA_FOLDER_PATH,
                    folder.relativePath
                )

                putExtra(
                    FolderMediaActivity.EXTRA_BUCKET,
                    folder.bucketName
                )
            }

        startActivityForResult(
            intent,
            REQUEST_FOLDER_MEDIA
        )
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
            requestCode ==
            REQUEST_COLOR &&
            resultCode ==
            RESULT_OK
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
            requestCode ==
            REQUEST_FOLDER_MEDIA &&
            resultCode ==
            RESULT_OK
        ) {

            val uri =
                data?.getStringExtra(
                    FolderMediaActivity.EXTRA_SELECTED_URI
                )

            if (
                !uri.isNullOrEmpty()
            ) {

                mediaUri =
                    uri

                sourceType =
                    "media"
            }

            return
        }

        if (
            requestCode ==
            REQUEST_FOLDER &&
            resultCode ==
            RESULT_OK
        ) {

            mediaUri =
                data?.data?.toString()

            sourceType =
                "folder"

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
            requestCode ==
            REQUEST_AUDIO
        ) {

            sourceType =
                "audio"

            return
        }

        val mime =
            contentResolver.getType(
                uri
            ) ?: ""

        sourceType =
            if (
                mime.startsWith(
                    "video/"
                )
            ) {

                "video"

            } else {

                "image"
            }
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

        startActivity(
            intent
        )

        finish()
    }

    data class MediaFolder(
        val name: String,
        val uri: Uri,
        val mediaType: Int,
        val relativePath: String?,
        val bucketName: String?,
        val latestFileName: String,
        val latestDate: Long
    )

    private inner class MediaFolderAdapter(
        private val items:
            List<MediaFolder>
    ) : RecyclerView.Adapter<
        MediaFolderAdapter.Holder
    >() {

        inner class Holder(
            val card: LinearLayout
        ) : RecyclerView.ViewHolder(
            card
        )

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): Holder {

            val card =
                LinearLayout(
                    parent.context
                ).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    gravity =
                        Gravity.CENTER

                    setPadding(
                        dp(6),
                        dp(22),
                        dp(6),
                        dp(6)
                    )

                    setBackgroundResource(
                        R.drawable.bg_folder_card
                    )
                }

            val image =
                ImageView(
                    parent.context
                ).apply {

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    setBackgroundColor(
                        Color.rgb(
                            10,
                            13,
                            18
                        )
                    )
                }

            card.addView(
                image,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(125)
                )
            )

            val title =
                TextView(
                    parent.context
                ).apply {

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        Color.WHITE
                    )

                    textSize =
                        13f

                    maxLines = 2
                }

            card.addView(
                title,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(38)
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

            return Holder(
                card
            )
        }

        override fun onBindViewHolder(
            holder: Holder,
            position: Int
        ) {

            val item =
                items[position]

            val image =
                holder.card.getChildAt(
                    0
                ) as ImageView

            val title =
                holder.card.getChildAt(
                    1
                ) as TextView

            title.text =
                item.name

            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )

            Thread {

                try {

                    if (
                        Build.VERSION.SDK_INT >= 29
                    ) {

                        val bitmap =
                            contentResolver.loadThumbnail(
                                item.uri,
                                android.util.Size(
                                    dp(350),
                                    dp(250)
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
                    }

                } catch (
                    _: Exception
                ) {
                }

            }.start()

            holder.card.setOnClickListener {

                openFolder(
                    item
                )
            }
        }

        override fun getItemCount():
            Int {

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
