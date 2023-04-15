package com.chocolatecake.todoapp.home.utils

import com.chocolatecake.todoapp.core.data.model.response.PersonalTask
import com.chocolatecake.todoapp.core.data.model.response.TeamTask
import com.chocolatecake.todoapp.home.model.HomeItem

fun TeamTask.toHomeItem(): HomeItem.TeamTaskItem {
    return HomeItem.TeamTaskItem(this)
}
fun PersonalTask.toHomeItem(): HomeItem.PersonalTaskItem {
    return HomeItem.PersonalTaskItem(this)
}