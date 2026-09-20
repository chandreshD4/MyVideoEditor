package com.myvideoeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myvideoeditor.data.ProjectStore
import com.myvideoeditor.editor.EditorActivity
import com.myvideoeditor.model.VideoProject
import com.myvideoeditor.ui.ProjectAdapter

class MainActivity : Activity() {

    private val PICK_VIDEO = 100

    private lateinit var projectStore: ProjectStore
    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        projectStore = ProjectStore(this)

        setupProjectsList()
        setupCreateButton()
    }

    private fun setupProjectsList() {

        val recyclerView = findViewById<RecyclerView>(
            R.id.projectsRecyclerView
        )

        projectAdapter = ProjectAdapter(
            projectStore.getProjects(),
            onProjectClick = { project ->
                openProject(project)
            }
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = projectAdapter
    }

    private fun setupCreateButton() {

        val createButton = findViewById<TextView>(
            R.id.createNewVideo
        )

        createButton.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

            startActivityForResult(
                intent,
                PICK_VIDEO
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (::projectStore.isInitialized &&
            ::projectAdapter.isInitialized
        ) {
            projectAdapter.updateProjects(
                projectStore.getProjects()
            )
        }
    }

    @Deprecated("Deprecated in Android API")
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
            requestCode == PICK_VIDEO &&
            resultCode == RESULT_OK
        ) {

            val videoUri = data?.data

            if (videoUri != null) {
                saveSelectedVideo(videoUri)
            }
        }
    }

    private fun saveSelectedVideo(
        videoUri: Uri
    ) {

        try {

            contentResolver.takePersistableUriPermission(
                videoUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

        } catch (_: SecurityException) {
        }

        val project = VideoProject(
            id = System.currentTimeMillis(),
            name = getVideoName(videoUri),
            videoUri = videoUri.toString()
        )

        projectStore.saveProject(project)

        projectAdapter.updateProjects(
            projectStore.getProjects()
        )

        openProject(project)
    }

    private fun openProject(
        project: VideoProject
    ) {

        val intent = Intent(
            this,
            EditorActivity::class.java
        ).apply {

            putExtra(
                EditorActivity.EXTRA_PROJECT_ID,
                project.id
            )

            putExtra(
                EditorActivity.EXTRA_PROJECT_NAME,
                project.name
            )

            putExtra(
                EditorActivity.EXTRA_VIDEO_URI,
                project.videoUri
            )
        }

        startActivity(intent)
    }

    private fun getVideoName(
        uri: Uri
    ): String {

        var name = "New Video"

        val cursor = contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME
            ),
            null,
            null,
            null
        )

        cursor?.use {

            if (it.moveToFirst()) {

                val index =
                    it.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (index >= 0) {
                    name = it.getString(index)
                }
            }
        }

        return name
    }
}
