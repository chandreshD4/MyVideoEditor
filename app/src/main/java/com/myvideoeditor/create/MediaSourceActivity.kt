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
        "Black"

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

    private var selectedFolder:
        MediaFolder? = null

    private var selectedFolderPosition =
        RecyclerView.NO_POSITION

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
                4
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
        }

        findViewById<TextView>(
            R.id.backgroundDone
        ).setOnClickListener {

            continueToEditor()
        }
    }

    private fun updateSeeAllButton() {

        if (folders.size <= 12) {

            seeAllButton.visibility =
                View.GONE

            return
        }

        seeAllButton.visibility =
            View.VISIBLE

        seeAllButton.text =
            if (showAllFolders) {
                "SHOW LESS"
            } else {
                "SEE ALL"
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

                selectedFolder =
                    null

                selectedFolderPosition =
                    RecyclerView.NO_POSITION

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
        bucketName: String?,
        relativePath: String?
    ): String {

        if (!relativePath.isNullOrBlank()) {

            val cleanPath =
                relativePath
                    .trimEnd('/')

            val lastSlash =
                cleanPath.lastIndexOf('/')

            if (lastSlash >= 0) {
                return cleanPath.substring(
                    lastSlash + 1
                )
            }

            if (cleanPath.isNotBlank()) {
                return cleanPath
            }
        }

        if (!bucketName.isNullOrBlank()) {
            return bucketName
        }

        return "Media"
    }

    private fun getFolderKey(
        bucketName: String?,
        relativePath: String?
    ): String {

        if (!relativePath.isNullOrBlank()) {
            return "path:$relativePath"
        }

        if (!bucketName.isNullOrBlank()) {
            return "bucket:$bucketName"
        }

        return "unknown"
    }

    private fun openMediaPicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                type = "video/*"

                putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    false
                )

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
            }

        try {

            startActivityForResult(
                intent,
                REQUEST_MEDIA
            )

        } catch (
            _: Exception
        ) {

            openImagePicker()
        }
    }

    private fun openImagePicker() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                type = "image/*"

                putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    false
                )

                addCategory(
                    Intent.CATEGORY_OPENABLE
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

                type = "audio/*"

                putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    false
                )

                addCategory(
                    Intent.CATEGORY_OPENABLE
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

                type = "*/*"

                putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    false
                )

                addCategory(
                    Intent.CATEGORY_OPENABLE
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
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                addFlags(
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

    private fun continueToEditor() {

        val selected =
            selectedFolder

        if (
            selected != null
        ) {

            mediaUri =
                selected.uri.toString()

            sourceType =
                if (
                    selected.mediaType ==
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                ) {
                    "video"
                } else {
                    "image"
                }
        }

        val intent =
            Intent(
                this,
                EditorActivity::class.java
            ).apply {

                putExtra(
                    EXTRA_ASPECT_RATIO,
                    aspectRatio
                )

                putExtra(
                    EXTRA_SOURCE_TYPE,
                    sourceType
                )

                putExtra(
                    EXTRA_BACKGROUND_COLOR,
                    selectedColor
                )

                putExtra(
                    EXTRA_MEDIA_URI,
                    mediaUri
                )

                putExtra(
                    EXTRA_PROJECT_NAME,
                    projectName
                )
            }

        startActivity(
            intent
        )

        finish()
    }

    private fun updateSelectedColor() {

        colorsBox.text =
            "●\nCOLORS\n$selectedColorName"

        colorsBox.setTextColor(
            getReadableTextColor(
                selectedColor
            )
        )

        colorsBox.background =
            roundedBackground(
                selectedColor,
                10
            )
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

    private inner class MediaFolderAdapter(
        private val items:
            List<MediaFolder>
    ) : RecyclerView.Adapter<
        MediaFolderAdapter.FolderHolder
    >() {

        inner class FolderHolder(
            val card: FrameLayout
        ) : RecyclerView.ViewHolder(
            card
        )

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FolderHolder {

            val card =
                FrameLayout(
                    parent.context
                )

            val params =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(112)
                )

            params.setMargins(
                dp(2),
                dp(3),
                dp(2),
                dp(3)
            )

            card.layoutParams =
                params

            val folderBody =
                FrameLayout(
                    parent.context
                ).apply {

                    background =
                        createFolderBackground(
                            folderColors[
                                viewType %
                                    folderColors.size
                            ]
                        )
                }

            val bodyParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dp(92)
                )

            bodyParams.topMargin =
                dp(18)

            bodyParams.leftMargin =
                dp(2)

            bodyParams.rightMargin =
                dp(2)

            bodyParams.bottomMargin =
                dp(2)

            card.addView(
                folderBody,
                bodyParams
            )

            val tab =
                View(
                    parent.context
                ).apply {

                    background =
                        createFolderTabBackground(
                            folderColors[
                                viewType %
                                    folderColors.size
                            ]
                        )
                }

            val tabParams =
                FrameLayout.LayoutParams(
                    dp(48),
                    dp(24)
                )

            tabParams.gravity =
                Gravity.TOP or Gravity.START

            tabParams.leftMargin =
                dp(2)

            card.addView(
                tab,
                tabParams
            )

            val image =
                ImageView(
                    parent.context
                ).apply {

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    setBackgroundColor(
                        Color.BLACK
                    )
                }

            val imageParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dp(58)
                )

            imageParams.leftMargin =
                dp(8)

            imageParams.rightMargin =
                dp(8)

            imageParams.topMargin =
                dp(25)

            folderBody.addView(
                image,
                imageParams
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
                        9f

                    maxLines =
                        1

                    ellipsize =
                        android.text.TextUtils.TruncateAt.END
                }

            val titleParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dp(24)
                )

            titleParams.gravity =
                Gravity.BOTTOM

            titleParams.leftMargin =
                dp(3)

            titleParams.rightMargin =
                dp(3)

            titleParams.bottomMargin =
                dp(2)

            folderBody.addView(
                title,
                titleParams
            )

            val check =
                TextView(
                    parent.context
                ).apply {

                    gravity =
                        Gravity.CENTER

                    text =
                        "✓"

                    textSize =
                        17f

                    setTextColor(
                        Color.WHITE
                    )

                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )

                    background =
                        roundedBackground(
                            Color.rgb(
                                0,
                                190,
                                100
                            ),
                            50
                        )

                    visibility =
                        View.GONE
                }

            val checkParams =
                FrameLayout.LayoutParams(
                    dp(30),
                    dp(30)
                )

            checkParams.gravity =
                Gravity.TOP or Gravity.END

            checkParams.topMargin =
                dp(21)

            checkParams.rightMargin =
                dp(2)

            card.addView(
                check,
                checkParams
            )

            return FolderHolder(
                card
            )
        }

        override fun onBindViewHolder(
            holder: FolderHolder,
            position: Int
        ) {

            val realPosition =
                holder.bindingAdapterPosition

            if (
                realPosition ==
                RecyclerView.NO_POSITION
            ) {
                return
            }

            val item =
                items[realPosition]

            val color =
                folderColors[
                    realPosition %
                        folderColors.size
                ]

            val folderBody =
                holder.card.getChildAt(
                    0
                ) as FrameLayout

            val tab =
                holder.card.getChildAt(
                    1
                )

            folderBody.background =
                createFolderBackground(
                    color
                )

            tab.background =
                createFolderTabBackground(
                    color
                )

            val image =
                folderBody.getChildAt(
                    0
                ) as ImageView

            val title =
                folderBody.getChildAt(
                    1
                ) as TextView

            val check =
                holder.card.getChildAt(
                    2
                ) as TextView

            title.text =
                item.name

            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )

            check.visibility =
                if (
                    selectedFolderPosition ==
                    realPosition
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            Thread {

                try {

                    if (
                        Build.VERSION.SDK_INT >= 29
                    ) {

                        val bitmap =
                            contentResolver.loadThumbnail(
                                item.uri,
                                android.util.Size(
                                    dp(180),
                                    dp(120)
                                ),
                                null
                            )

                        runOnUiThread {

                            if (
                                holder.bindingAdapterPosition ==
                                realPosition
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

            folderBody.setOnClickListener {

                selectFolder(
                    realPosition
                )
            }

            tab.setOnClickListener {

                openFolder(
                    item
                )
            }

            image.setOnClickListener {

                selectFolder(
                    realPosition
                )
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
                    12,
                    items.size
                )
            }
        }
    }

    private fun selectFolder(
        position: Int
    ) {

        if (
            position <
            0 ||
            position >= folders.size
        ) {
            return
        }

        val oldPosition =
            selectedFolderPosition

        selectedFolder =
            folders[position]

        selectedFolderPosition =
            position

        if (
            oldPosition !=
            RecyclerView.NO_POSITION
        ) {

            mediaRecycler.adapter
                ?.notifyItemChanged(
                    oldPosition
                )
        }

        mediaRecycler.adapter
            ?.notifyItemChanged(
                position
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

    private fun createFolderBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadii =
                floatArrayOf(
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat(),
                    dp(3).toFloat()
                )
        }
    }

    private fun createFolderTabBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadii =
                floatArrayOf(
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    dp(10).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
                )
        }
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

                if (
                    uri != null
                ) {

                    mediaUri =
                        uri.toString()

                    sourceType =
                        "media"
                }
            }

            REQUEST_AUDIO -> {

                val uri =
                    data.data

                if (
                    uri != null
                ) {

                    mediaUri =
                        uri.toString()

                    sourceType =
                        "audio"
                }
            }

            REQUEST_FOLDER -> {

                val uri =
                    data.data

                if (
                    uri != null
                ) {

                    mediaUri =
                        uri.toString()

                    sourceType =
                        "folder"
                }
            }

            REQUEST_COLOR -> {

                selectedColor =
                    data.getIntExtra(
                        ColorsActivity.EXTRA_COLOR,
                        Color.BLACK
                    )

                selectedColorName =
                    data.getStringExtra(
                        ColorsActivity.EXTRA_COLOR_NAME
                    ) ?: "Black"

                updateSelectedColor()
            }

            REQUEST_FOLDER_MEDIA -> {

                val uri =
                    data.getStringExtra(
                        FolderMediaActivity.EXTRA_SELECTED_URI
                    )

                if (
                    !uri.isNullOrBlank()
                ) {

                    mediaUri =
                        uri

                    sourceType =
                        "media"

                    selectedFolder =
                        null

                    selectedFolderPosition =
                        RecyclerView.NO_POSITION

                    mediaRecycler.adapter
                        ?.notifyDataSetChanged()
                }
            }
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

        val relativePath:
            String?,

        val bucketName:
            String?,

        val latestFileName:
            String,

        val latestDate:
            Long
    )
}
