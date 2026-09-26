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
import android.view.ViewGroup
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

    private lateinit var recyclerView: RecyclerView

    private lateinit var emptyText: TextView

    private val mediaItems =
        ArrayList<FolderMedia>()

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
        ).text = folderName

        emptyText =
            findViewById(
                R.id.folderEmpty
            )

        recyclerView =
            findViewById(
                R.id.folderRecycler
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

        loadFolderMedia()
    }

    private fun loadFolderMedia() {

        emptyText.text =
            "Loading media..."

        Thread {

            val result =
                queryFolderMedia()

            runOnUiThread {

                mediaItems.clear()

                mediaItems.addAll(
                    result
                )

                recyclerView.adapter
                    ?.notifyDataSetChanged()

                if (
                    mediaItems.isEmpty()
                ) {

                    emptyText.visibility =
                        TextView.VISIBLE

                    emptyText.text =
                        "No photos or videos found"

                } else {

                    emptyText.visibility =
                        TextView.GONE
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
        val selectionArgs: Array<String>

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
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
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
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                    bucketName!!
                )

        } else {

            selection =
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"

            selectionArgs =
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
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
                            )

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
                            name,
                            itemUri,
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

    private inner class FolderMediaAdapter(
        private val items:
            List<FolderMedia>
    ) : RecyclerView.Adapter<
        FolderMediaAdapter.Holder
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
                        dp(5),
                        dp(22),
                        dp(5),
                        dp(5)
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
                        11f

                    maxLines = 2

                    setPadding(
                        dp(2),
                        dp(5),
                        dp(2),
                        dp(2)
                    )
                }

            card.addView(
                title,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(34)
                )
            )

            val params =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(155)
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
                                Size(
                                    dp(250),
                                    dp(180)
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

                val result =
                    Intent().apply {

                        putExtra(
                            EXTRA_SELECTED_URI,
                            item.uri.toString()
                        )
                    }

                setResult(
                    RESULT_OK,
                    result
                )

                finish()
            }
        }

        override fun getItemCount():
            Int {

            return items.size
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
