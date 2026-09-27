package com.myvideoeditor.create

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myvideoeditor.R

class FolderMediaActivity : Activity() {

    companion object {

        const val EXTRA_FOLDER_NAME =
            "folder_name"

        const val EXTRA_FOLDER_PATH =
            "folder_path"

        const val EXTRA_BUCKET =
            "bucket_name"

        const val EXTRA_SELECTED_URI =
            "selected_uri"
    }

    private lateinit var recyclerView:
        RecyclerView

    private lateinit var emptyText:
        TextView

    private lateinit var continueButton:
        TextView

    private val mediaItems =
        ArrayList<FolderMedia>()

    private var selectedPosition =
        RecyclerView.NO_POSITION

    private var folderName =
        "Media"

    private var folderPath:
        String? = null

    private var bucketName:
        String? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_folder_media
        )

        folderName =
            intent.getStringExtra(
                EXTRA_FOLDER_NAME
            ) ?: "Media"

        folderPath =
            intent.getStringExtra(
                EXTRA_FOLDER_PATH
            )

        bucketName =
            intent.getStringExtra(
                EXTRA_BUCKET
            )

        findViewById<TextView>(
            R.id.folderBack
        ).setOnClickListener {
            finish()
        }

        findViewById<TextView>(
            R.id.folderTitle
        ).text =
            folderName

        emptyText =
            findViewById(
                R.id.folderEmpty
            )

        recyclerView =
            findViewById(
                R.id.folderRecycler
            )

        continueButton =
            findViewById(
                R.id.folderContinue
            )

        recyclerView.layoutManager =
            GridLayoutManager(
                this,
                3
            )

        recyclerView.isNestedScrollingEnabled =
            false

        recyclerView.adapter =
            FolderMediaAdapter(
                mediaItems
            )

        continueButton.setOnClickListener {
            continueWithSelectedMedia()
        }

        loadFolderMedia()
    }

    private fun loadFolderMedia() {

        emptyText.text =
            "Loading media..."

        emptyText.visibility =
            View.VISIBLE

        Thread {

            val result =
                queryFolderMedia()

            runOnUiThread {

                mediaItems.clear()

                mediaItems.addAll(
                    result
                )

                selectedPosition =
                    RecyclerView.NO_POSITION

                recyclerView.adapter
                    ?.notifyDataSetChanged()

                if (
                    mediaItems.isEmpty()
                ) {

                    emptyText.visibility =
                        View.VISIBLE

                    emptyText.text =
                        "No photos or videos found"

                } else {

                    emptyText.visibility =
                        View.GONE
                }
            }

        }.start()
    }

    private fun queryFolderMedia():
        ArrayList<FolderMedia> {

        val result =
            ArrayList<FolderMedia>()

        val projection =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED
            )

        val mediaUri =
            MediaStore.Files.getContentUri(
                "external"
            )

        val selection: String

        val selectionArgs:
            Array<String>

        if (
            Build.VERSION.SDK_INT >= 29 &&
            !folderPath.isNullOrBlank()
        ) {

            selection =
                "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND " +
                    "${MediaStore.MediaColumns.RELATIVE_PATH}=?"

            selectionArgs =
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        .toString(),

                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        .toString(),

                    folderPath!!
                )

        } else if (
            !bucketName.isNullOrBlank()
        ) {

            selection =
                "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND " +
                    "${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME}=?"

            selectionArgs =
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        .toString(),

                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        .toString(),

                    bucketName!!
                )

        } else {

            selection =
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"

            selectionArgs =
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        .toString(),

                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        .toString()
                )
        }

        try {

            contentResolver.query(
                mediaUri,
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

                    val type =
                        cursor.getInt(
                            typeIndex
                        )

                    val name =
                        if (
                            nameIndex >= 0
                        ) {
                            cursor.getString(
                                nameIndex
                            ) ?: "Media"
                        } else {
                            "Media"
                        }

                    val itemUri =
                        if (
                            type ==
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
                        FolderMedia(
                            name =
                                name,

                            uri =
                                itemUri,

                            mediaType =
                                type
                        )
                    )
                }
            }

        } catch (
            _: Exception
        ) {
        }

        return result
    }

    private fun continueWithSelectedMedia() {

        if (
            selectedPosition ==
            RecyclerView.NO_POSITION
        ) {

            emptyText.visibility =
                View.VISIBLE

            emptyText.text =
                "Please select a photo or video"

            return
        }

        if (
            selectedPosition >=
            mediaItems.size
        ) {
            return
        }

        val selected =
            mediaItems[
                selectedPosition
            ]

        val result =
            Intent().apply {

                putExtra(
                    EXTRA_SELECTED_URI,
                    selected.uri.toString()
                )
            }

        setResult(
            RESULT_OK,
            result
        )

        finish()
    }

    private inner class FolderMediaAdapter(
        private val items:
            List<FolderMedia>
    ) : RecyclerView.Adapter<
        FolderMediaAdapter.Holder
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

            val image =
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

            card.addView(
                image,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(88)
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
                        10f

                    maxLines =
                        1

                    ellipsize =
                        android.text.TextUtils.TruncateAt.END

                    setBackgroundColor(
                        Color.rgb(
                            20,
                            24,
                            31
                        )
                    )
                }

            val titleParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(24)
                )

            titleParams.gravity =
                Gravity.BOTTOM

            card.addView(
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

                   setTextColor (
                        Color.WHITE
                    )

                    textSize =
                        16f

                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )

                    background =
                        createCheckBackground()

                    visibility =
                        View.GONE
                }

            val checkParams =
                FrameLayout.LayoutParams(
                    dp(30),
                    dp(30)
                )

            checkParams.gravity =
                Gravity.TOP or
                    Gravity.END

            checkParams.setMargins(
                0,
                dp(5),
                dp(5),
                0
            )

            card.addView(
                check,
                checkParams
            )

            val params =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(118)
                )

            params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
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

            val check =
                holder.card.getChildAt(
                    2
                ) as TextView

            title.text =
                item.name

            check.visibility =
                if (
                    position ==
                    selectedPosition
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

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
                                Size(
                                    dp(220),
                                    dp(160)
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

                val oldPosition =
                    selectedPosition

                selectedPosition =
                    holder.bindingAdapterPosition

                if (
                    oldPosition !=
                    RecyclerView.NO_POSITION
                ) {

                    notifyItemChanged(
                        oldPosition
                    )
                }

                notifyItemChanged(
                    selectedPosition
                )
            }
        }

        override fun getItemCount():
            Int {

            return items.size
        }
    }

    private fun createCheckBackground():
        android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable().apply {

            shape =
                android.graphics.drawable.GradientDrawable.OVAL

            setColor(
                Color.rgb(
                    20,
                    190,
                    95
                )
            )

            setStroke(
                dp(2),
                Color.WHITE
            )
        }
    }

    data class FolderMedia(
        val name: String,
        val uri: Uri,
        val mediaType: Int
    )

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
