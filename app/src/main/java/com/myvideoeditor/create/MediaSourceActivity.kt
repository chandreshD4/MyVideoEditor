package com.myvideoeditor.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    private var aspectRatio =
        "9:16"

    private var sourceType =
        "blank"

    private var mediaUri:
        String? = null

    private var projectName =
        "New Project"

    private var selectedColor =
        Color.BLACK

    private var selectedColorName =
        ""

    private var hasSelectedColor =
        false

    private lateinit var colorsBox:
        TextView

    private lateinit var mediaEmptyText:
        TextView

    private lateinit var mediaRecycler:
        RecyclerView

    private lateinit var seeAllButton:
        TextView

    private val folders =
        ArrayList<MediaFolder>()

    private var showAllFolders =
        false

    private val folderColors =
        listOf(
            Color.rgb(244, 67, 54),
            Color.rgb(233, 30, 99),
            Color.rgb(156, 39, 176),
            Color.rgb(63, 81, 181),
            Color.rgb(33, 150, 243),
            Color.rgb(0, 188, 212),
            Color.rgb(0, 150, 136),
            Color.rgb(76, 175, 80),
            Color.rgb(139, 195, 74),
            Color.rgb(255, 193, 7),
            Color.rgb(255, 152, 0),
            Color.rgb(255, 87, 34)
        )

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

        seeAllButton =
            findViewById(
                R.id.seeAllButton
            )

        setupRecycler()
        setupClicks()
        updateSelectedColor()
        updateSeeAllButton()
        requestMediaPermission()
    }

    private fun setupRecycler() {

        mediaRecycler.layoutManager =
            GridLayoutManager(
                this,
                3
            )

        mediaRecycler.isNestedScrollingEnabled =
            true

        mediaRecycler.setHasFixedSize(
            false
        )

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

        findViewById<TextView>(
            R.id.importButton
        ).setOnClickListener {
            openMediaPicker()
        }

        colorsBox.setOnClickListener {
            openColorsScreen()
        }

        findViewById<TextView>(
            R.id.mediaDevice
        ).setOnClickListener {
            openAllFiles()
        }

        findViewById<TextView>(
            R.id.mediaPhotos
        ).setOnClickListener {
            openImagePicker()
        }

        findViewById<TextView>(
            R.id.boxFive
        ).setOnClickListener {
            openAudioPicker()
        }

        findViewById<TextView>(
            R.id.boxSix
        ).setOnClickListener {
            openFolderPicker()
        }

        seeAllButton.setOnClickListener {

            showAllFolders =
                !showAllFolders

            updateSeeAllButton()

            mediaRecycler.adapter
                ?.notifyDataSetChanged()

            mediaRecycler.post {

                mediaRecycler.scrollToPosition(
                    0
                )
            }
        }

        findViewById<TextView>(
            R.id.backgroundDone
        ).setOnClickListener {
            continueToEditor()
        }
    }

    private fun updateSeeAllButton() {

        if (
            folders.size <= 9
        ) {

            seeAllButton.visibility =
                View.GONE

            return
        }

        seeAllButton.visibility =
            View.VISIBLE

        seeAllButton.text =
            if (
                showAllFolders
            ) {

                "SHOW LESS  ↑"

            } else {

                "SEE ALL  ↓"
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
            View.VISIBLE

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

                showAllFolders =
                    false

                mediaRecycler.adapter
                    ?.notifyDataSetChanged()

                updateSeeAllButton()

                if (
                    folders.isEmpty()
                ) {

                    mediaEmptyText.visibility =
                        View.VISIBLE

                    mediaEmptyText.text =
                        "No photos or videos found"

                } else {

                    mediaEmptyText.visibility =
                        View.GONE
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
                            name =
                                folderName,

                            uri =
                                itemUri,

                            mediaType =
                                mediaType,

                            relativePath =
                                relativePath,

                            bucketName =
                                bucket,

                            latestFileName =
                                displayName,

                            latestDate =
                                dateAdded
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
        bucketName: String?,
        relativePath: String?
    ): String {

        if (
            !relativePath.isNullOrBlank()
        ) {

            val cleanPath =
                relativePath.trimEnd(
                    '/'
                )

            val lastSlash =
                cleanPath.lastIndexOf(
                    '/'
                )

            if (
                lastSlash >= 0
            ) {

                return cleanPath.substring(
                    lastSlash + 1
                )
            }

            if (
                cleanPath.isNotBlank()
            ) {

                return cleanPath
            }
        }

        if (
            !bucketName.isNullOrBlank()
        ) {

            return bucketName
        }

        return "Media"
    }

    private fun getFolderKey(
        bucketName: String?,
        relativePath: String?
    ): String {

        if (
            !relativePath.isNullOrBlank()
        ) {

            return "path:$relativePath"
                .lowercase()
        }

        if (
            !bucketName.isNullOrBlank()
        ) {

            return "bucket:$bucketName"
                .lowercase()
        }

        return "unknown"
    }

    private fun openMediaPicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type =
                    "*/*"

                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "image/*",
                        "video/*"
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

                type =
                    "image/*"

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

                type =
                    "audio/*"

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

                type =
                    "*/*"

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
            ).apply {

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            REQUEST_FOLDER
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

    private fun updateSelectedColor() {

        if (
            !hasSelectedColor ||
            selectedColorName.isBlank()
        ) {

            colorsBox.text =
                "●\nCOLORS\nChoose"

            colorsBox.background =
                GradientDrawable().apply {

                    setColor(
                        Color.rgb(
                            48,
                            39,
                            25
                        )
                    )

                    cornerRadius =
                        dp(10).toFloat()
                }

            colorsBox.setTextColor(
                Color.rgb(
                    255,
                    215,
                    90
                )
            )

            return
        }

        colorsBox.text =
            "●\nCOLORS\n$selectedColorName"

        colorsBox.background =
            GradientDrawable().apply {

                setColor(
                    selectedColor
                )

                cornerRadius =
                    dp(10).toFloat()
            }

        colorsBox.setTextColor(
            getReadableTextColor(
                selectedColor
            )
        )
    }

    private fun clearMediaSelection() {

        mediaUri =
            null

        sourceType =
            "blank"
    }

    private fun clearColorSelection() {

        selectedColor =
            Color.BLACK

        selectedColorName =
            ""

        hasSelectedColor =
            false

        updateSelectedColor()
    }

    private fun continueToEditor() {

        val hasMedia =
            !mediaUri.isNullOrBlank()

        if (
            !hasMedia &&
            !hasSelectedColor
        ) {

            Toast.makeText(
                this,
                "Please select a photo, video, or background color",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        openEditor()
    }

    private fun openEditor() {

        val hasMedia =
            !mediaUri.isNullOrBlank()

        val intent =
            Intent(
                this,
                EditorActivity::class.java
            ).apply {

                putExtra(
                    EditorActivity.EXTRA_PROJECT_NAME,
                    projectName
                )

                putExtra(
                    EditorActivity.EXTRA_ASPECT_RATIO,
                    aspectRatio
                )

                putExtra(
                    EditorActivity.EXTRA_SOURCE_TYPE,
                    if (
                        hasMedia
                    ) {

                        sourceType

                    } else {

                        "blank"
                    }
                )

                putExtra(
                    EditorActivity.EXTRA_BACKGROUND_COLOR,
                    if (
                        hasSelectedColor
                    ) {

                        selectedColor

                    } else {

                        Color.BLACK
                    }
                )

                if (
                    hasMedia
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
            resultCode !=
            RESULT_OK ||
            data == null
        ) {

            return
        }

        when (
            requestCode
        ) {

            REQUEST_MEDIA -> {

                val uri =
                    data.data
                        ?: return

                try {

                    contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (
                    _: Exception
                ) {
                }

                clearColorSelection()

                mediaUri =
                    uri.toString()

                sourceType =
                    getSourceType(
                        uri
                    )

                openEditor()
            }

            REQUEST_AUDIO -> {

                val uri =
                    data.data
                        ?: return

                try {

                    contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (
                    _: Exception
                ) {
                }

                clearColorSelection()

                mediaUri =
                    uri.toString()

                sourceType =
                    "audio"

                openEditor()
            }

            REQUEST_COLOR -> {

                val colorName =
                    data.getStringExtra(
                        ColorsActivity.EXTRA_COLOR_NAME
                    )

                if (
                    colorName.isNullOrBlank()
                ) {

                    return
                }

                selectedColor =
                    data.getIntExtra(
                        ColorsActivity.EXTRA_COLOR,
                        Color.BLACK
                    )

                selectedColorName =
                    colorName

                hasSelectedColor =
                    true

                clearMediaSelection()

                updateSelectedColor()

                openEditor()
            }

            REQUEST_FOLDER_MEDIA -> {

                val uriString =
                    data.getStringExtra(
                        FolderMediaActivity.EXTRA_SELECTED_URI
                    )
                        ?: return

                val uri =
                    Uri.parse(
                        uriString
                    )

                clearColorSelection()

                mediaUri =
                    uriString

                sourceType =
                    getSourceType(
                        uri
                    )

                openEditor()
            }

            REQUEST_FOLDER -> {

                val folderUri =
                    data.data

                if (
                    folderUri != null
                ) {

                    try {

                        contentResolver
                            .takePersistableUriPermission(
                                folderUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                    } catch (
                        _: Exception
                    ) {
                    }

                    clearColorSelection()

                    clearMediaSelection()

                    Toast.makeText(
                        this,
                        "Folder added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getSourceType(
        uri: Uri
    ): String {

        val mime =
            try {

                contentResolver.getType(
                    uri
                )

            } catch (
                _: Exception
            ) {

                null
            }

        return if (
            mime?.startsWith(
                "video/"
            ) == true
        ) {

            "video"

        } else {

            "image"
        }
    }

    private inner class MediaFolderAdapter(
        private val items:
            List<MediaFolder>
    ) : RecyclerView.Adapter<
        MediaFolderAdapter.Holder
    >() {

        inner class Holder(
            val card: FrameLayout
        ) : RecyclerView.ViewHolder(
            card
        )

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): Holder {

            val card =
                FrameLayout(
                    parent.context
                )

            val body =
                FrameLayout(
                    parent.context
                ).apply {

                    background =
                        createFolderBody(
                            folderColors[
                                viewType %
                                    folderColors.size
                            ]
                        )
                }

            val bodyParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(72)
                )

            bodyParams.topMargin =
                dp(14)

            card.addView(
                body,
                bodyParams
            )

            val thumbnail =
                ImageView(
                    parent.context
                ).apply {

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    setBackgroundColor(
                        Color.rgb(
                            12,
                            15,
                            20
                        )
                    )
                }

            val imageParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(42)
                )

            imageParams.leftMargin =
                dp(4)

            imageParams.rightMargin =
                dp(4)

            imageParams.topMargin =
                dp(18)

            body.addView(
                thumbnail,
                imageParams
            )

            val title =
                TextView(
                    parent.context
                ).apply {

                    gravity =
                        Gravity.CENTER

                    includeFontPadding =
                        false

                    setTextColor(
                        Color.WHITE
                    )

                    textSize =
                        8f

                    maxLines =
                        1

                    ellipsize =
                        android.text.TextUtils.TruncateAt.END
                }

            val titleParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(18)
                )

            titleParams.gravity =
                Gravity.BOTTOM

            body.addView(
                title,
                titleParams
            )

            val tab =
                View(
                    parent.context
                ).apply {

                    background =
                        createFolderTab(
                            folderColors[
                                viewType %
                                    folderColors.size
                            ]
                        )
                }

            val tabParams =
                FrameLayout.LayoutParams(
                    dp(34),
                    dp(10)
                )

            tabParams.gravity =
                Gravity.TOP or
                    Gravity.START

            card.addView(
                tab,
                tabParams
            )

            val params =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(98)
                )

            params.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
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

            val body =
                holder.card.getChildAt(
                    0
                ) as FrameLayout

            val thumbnail =
                body.getChildAt(
                    0
                ) as ImageView

            val title =
                body.getChildAt(
                    1
                ) as TextView

            val color =
                folderColors[
                    position %
                        folderColors.size
                ]

            body.background =
                createFolderBody(
                    color
                )

            val tab =
                holder.card.getChildAt(
                    1
                )

            tab.background =
                createFolderTab(
                    color
                )

            title.text =
                item.name

            thumbnail.setImageResource(
                android.R.drawable.ic_menu_gallery
            )

            thumbnail.tag =
                item.uri.toString()

            Thread {

                try {

                    if (
                        Build.VERSION.SDK_INT >= 29
                    ) {

                        val bitmap =
                            contentResolver
                                .loadThumbnail(
                                    item.uri,
                                    android.util.Size(
                                        dp(180),
                                        dp(120)
                                    ),
                                    null
                                )

                        runOnUiThread {

                            if (
                                thumbnail.tag ==
                                item.uri.toString()
                            ) {

                                thumbnail.setImageBitmap(
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

                val clickedPosition =
                    holder.bindingAdapterPosition

                if (
                    clickedPosition ==
                    RecyclerView.NO_POSITION
                ) {

                    return@setOnClickListener
                }

                if (
                    clickedPosition <
                    items.size
                ) {

                    openFolder(
                        items[
                            clickedPosition
                        ]
                    )
                }
            }
        }

        override fun getItemCount():
            Int {

            return if (
                showAllFolders
            ) {

                items.size

            } else {

                minOf(
                    9,
                    items.size
                )
            }
        }

        override fun getItemViewType(
            position: Int
        ): Int {

            return position %
                folderColors.size
        }
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
                    dp(8).toFloat(),
                    dp(8).toFloat(),
                    dp(8).toFloat(),
                    dp(8).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
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

    data class MediaFolder(
        val name: String,
        val uri: Uri,
        val mediaType: Int,
        val relativePath: String?,
        val bucketName: String?,
        val latestFileName: String,
        val latestDate: Long
    )
}
