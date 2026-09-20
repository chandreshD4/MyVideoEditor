package com.myvideoeditor.data

import android.content.Context
import com.myvideoeditor.model.VideoProject
import org.json.JSONArray
import org.json.JSONObject

class ProjectStore(context: Context) {

    private val preferences =
        context.getSharedPreferences("kc_editor_projects", Context.MODE_PRIVATE)

    fun getProjects(): MutableList<VideoProject> {
        val result = mutableListOf<VideoProject>()
        val json = preferences.getString("projects", "[]") ?: "[]"

        val array = JSONArray(json)

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            val thumbnailValue = item.optString("thumbnailUri", "")

            result.add(
                VideoProject(
                    id = item.getLong("id"),
                    name = item.getString("name"),
                    videoUri = item.getString("videoUri"),
                    thumbnailUri = thumbnailValue.ifEmpty { null },
                    duration = item.optLong("duration", 0L),
                    lastEdited = item.optLong(
                        "lastEdited",
                        System.currentTimeMillis()
                    ),
                    isFinished = item.optBoolean("isFinished", false)
                )
            )
        }

        return result
    }

    fun saveProject(project: VideoProject) {
        val projects = getProjects()

        projects.removeAll { it.id == project.id }
        projects.add(0, project)

        saveAll(projects)
    }

    fun deleteProject(projectId: Long) {
        val projects = getProjects()

        projects.removeAll { it.id == projectId }

        saveAll(projects)
    }

    private fun saveAll(projects: List<VideoProject>) {
        val array = JSONArray()

        projects.forEach { project ->
            val item = JSONObject()

            item.put("id", project.id)
            item.put("name", project.name)
            item.put("videoUri", project.videoUri)
            item.put("thumbnailUri", project.thumbnailUri)
            item.put("duration", project.duration)
            item.put("lastEdited", project.lastEdited)
            item.put("isFinished", project.isFinished)

            array.put(item)
        }

        preferences.edit()
            .putString("projects", array.toString())
            .apply()
    }
}
