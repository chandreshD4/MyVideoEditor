package com.myvideoeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myvideoeditor.create.FormatActivity
import com.myvideoeditor.data.ProjectStore
import com.myvideoeditor.editor.EditorActivity
import com.myvideoeditor.model.VideoProject
import com.myvideoeditor.ui.ProjectAdapter

class MainActivity : Activity() {

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

        val recyclerView =
            findViewById<RecyclerView>(
                R.id.projectsRecyclerView
            )

        projectAdapter =
            ProjectAdapter(
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

        val createButton =
            findViewById<TextView>(
                R.id.createNewVideo
            )

        createButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    FormatActivity::class.java
                )

            startActivity(intent)
        }
    }

    override fun onResume() {

        super.onResume()

        if (
            ::projectStore.isInitialized &&
            ::projectAdapter.isInitialized
        ) {

            projectAdapter.updateProjects(
                projectStore.getProjects()
            )
        }
    }

    private fun openProject(
        project: VideoProject
    ) {

        val intent =
            Intent(
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
}
