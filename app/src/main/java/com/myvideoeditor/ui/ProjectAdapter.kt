package com.myvideoeditor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myvideoeditor.R
import com.myvideoeditor.model.VideoProject

class ProjectAdapter(
    private val projects: MutableList<VideoProject>,
    private val onProjectClick: (VideoProject) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.projectName)
        val details: TextView = view.findViewById(R.id.projectDetails)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)

        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProjectViewHolder,
        position: Int
    ) {
        val project = projects[position]

        holder.name.text = project.name
        holder.details.text =
            if (project.isFinished) "Finished project"
            else "Tap to continue editing"

        holder.itemView.setOnClickListener {
            onProjectClick(project)
        }
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    fun updateProjects(newProjects: List<VideoProject>) {
        projects.clear()
        projects.addAll(newProjects)
        notifyDataSetChanged()
    }
}
