package com.chocolatecake.todoapp.add_new_task.presenter

import android.content.Context
import com.chocolatecake.todoapp.core.data.network.services.personal.PersonalTaskService
import com.chocolatecake.todoapp.core.data.network.services.team.TeamTaskService
import com.chocolatecake.todoapp.add_new_task.view.AddNewTaskView
import com.chocolatecake.todoapp.core.data.local.TaskSharedPreferences
import com.chocolatecake.todoapp.core.data.model.request.PersonalTaskRequest
import com.chocolatecake.todoapp.core.data.model.request.TeamTaskRequest

class AddNewTaskPresenter(private val context: Context,private val addNewTaskView: AddNewTaskView) {
    private var sharedPreferences = TaskSharedPreferences()
        .also { it.initPreferences(context) }
    private val personalTaskService = PersonalTaskService(sharedPreferences)
    private val teamTaskService = TeamTaskService(sharedPreferences)

    fun createPersonalTask(title: String, description: String){
        val personalTaskRequest =
            PersonalTaskRequest(
                title,
                description
            )
        personalTaskService.createTask(
            personalTaskRequest,
            onFailure = { addNewTaskView.onCreateTaskFailure() },
            onSuccess = { addNewTaskView.onCreateTaskSuccess() }
        )
    }

    fun createTeamTask(title: String, description: String, assignee: String) {
        val teamTaskRequest = TeamTaskRequest(
            title,
            description,
            assignee
        )
        teamTaskService.createTask(
            teamTaskRequest,
            onFailure = { addNewTaskView.onCreateTaskFailure() },
            onSuccess = { addNewTaskView.onCreateTaskSuccess() }
        )
    }
}