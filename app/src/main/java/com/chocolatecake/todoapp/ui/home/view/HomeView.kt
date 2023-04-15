package com.chocolatecake.todoapp.ui.home.view

import com.chocolatecake.todoapp.data.model.response.PersonalTask
import com.chocolatecake.todoapp.data.model.response.TeamTask
import com.chocolatecake.todoapp.ui.home.model.Status

interface HomeView {
    fun onAllTasksFailure(message: String?)
    fun onTeamTasksSuccess(teamTasks: List<TeamTask>)
    fun onPersonalTasksSuccess(personalTasks: List<PersonalTask>)
    fun onUnauthorizedResponse()

    fun onSearchTeamResultSuccess(teamTasks: List<TeamTask>)
    fun onSearchPersonalResultSuccess(personalTasks: List<PersonalTask>)
    fun onStatusCountsSuccess(statusList: Triple<Int?, Int?, Int?>)
}