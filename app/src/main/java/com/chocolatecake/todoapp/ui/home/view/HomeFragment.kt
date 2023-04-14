package com.chocolatecake.todoapp.ui.home.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.forEachIndexed
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.chocolatecake.todoapp.R
import com.chocolatecake.todoapp.data.model.response.PersonalTask
import com.chocolatecake.todoapp.data.model.response.TeamTask
import com.chocolatecake.todoapp.databinding.FragmentHomeBinding
import com.chocolatecake.todoapp.ui.base.fragment.BaseFragment
import com.chocolatecake.todoapp.ui.home.adapter.HomeAdapter
import com.chocolatecake.todoapp.ui.home.model.HomeItem
import com.chocolatecake.todoapp.ui.home.model.SearchQuery
import com.chocolatecake.todoapp.ui.home.model.Status
import com.chocolatecake.todoapp.ui.home.presenter.HomePresenter
import com.chocolatecake.todoapp.ui.home.utils.toHomeItem
import com.chocolatecake.todoapp.ui.login.LoginFragment
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout

class HomeFragment : BaseFragment<FragmentHomeBinding>(), HomeView {
    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    private val presenter by lazy { HomePresenter(this, requireContext()) }
    private lateinit var homeAdapter: HomeAdapter
    private var searchQuery: SearchQuery = SearchQuery()
    private var isPersonal = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getTeamTask(getSelectedChips())
        addCallBacks()
    }

    private fun addCallBacks() {
        binding.floatingActionButton.setOnClickListener {
//            val addNewTaskFragment = AddNewTaskFragment.newInstance(true)
//            replaceFragment(addNewTaskFragment)
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.apply {
                    when (tab.position) {
                        TEAM_POSITION -> {
                            presenter.getTeamTask(getSelectedChips())
                            isPersonal = false
                        }
                        PERSONAL_POSITION -> {
                            presenter.getPersonalTask(getSelectedChips())
                            isPersonal = true
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.editTextSearch.addTextChangedListener {
            searchQuery = searchQuery.copy(
                title = it.toString(),
                status = getSelectedChips()
            )
            if (isPersonal) {
                presenter.searchPersonalTasks(searchQuery)
            } else {
                presenter.searchTeamTasks(searchQuery)
            }
        }
    }

    private fun getSelectedChips(): List<Status> {
        val statusList = mutableListOf<Status>()
        binding.chipGroup.forEachIndexed { index, view ->
            if ((view as Chip).isChecked) {
                statusList.add(Status.createStatus(index))
            }
        }
        return statusList.toList()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_view, fragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onAllTasksFailure(message: String?) {
        createToast(message)
    }

    override fun onTeamTasksSuccess(teamTasks: List<TeamTask>) {
        activity?.runOnUiThread {
            setUpTeamTasksRecyclerView(teamTasks)
        }
    }

    override fun onPersonalTasksSuccess(personalTasks: List<PersonalTask>) {
        activity?.runOnUiThread {
            setUpPersonalTasksRecyclerView(personalTasks)
        }
    }

    override fun onUnauthorizedResponse() {
        replaceFragment(LoginFragment())
    }

    override fun onSearchTeamResultSuccess(teamTasks: List<TeamTask>) {
        activity?.runOnUiThread {
//            setUpTeamTasksRecyclerView(teamTasks)
            val itemsList: MutableList<HomeItem> = mutableListOf()
            itemsList.addAll(teamTasks.map { it.toHomeItem() })
            homeAdapter.updateList(itemsList)
        }
    }

    override fun onSearchPersonalResultSuccess(personalTasks: List<PersonalTask>) {
//        val itemsList: MutableList<HomeItem> = mutableListOf()
//        itemsList.addAll(personalTasks.map { it.toHomeItem() })
//        activity?.runOnUiThread {
//            homeAdapter.setSelectedTabData(itemsList)
//        }
    }

    private fun setUpTeamTasksRecyclerView(teamTasks: List<TeamTask>) {
        Log.e("mine", teamTasks.toString())
        val tasksCount = getTeamTasksCount(teamTasks)
        updateChipsStatus(tasksCount)
        val itemsList: MutableList<HomeItem> = mutableListOf()
        itemsList.addAll(teamTasks.map { it.toHomeItem() })
        homeAdapter =
            HomeAdapter(itemsList, ::onClickTask, ::onClickTask)
        binding.recyclerView.adapter = homeAdapter
        homeAdapter.updateList(itemsList)

    }

    private fun updateChipsStatus(tasksCount: List<Int>) {
        binding.chipGroup.forEachIndexed { index, view ->
            (view as Chip).text = String.format(view.text.toString(), tasksCount[index])
        }
    }

    private fun setUpPersonalTasksRecyclerView(personalTasks: List<PersonalTask>) {
        Log.e("mine", personalTasks.toString())
        val tasksCount = getPersonalTasksCount(personalTasks)
        updateChipsStatus(tasksCount)
        val itemsList: MutableList<HomeItem> = mutableListOf()
        itemsList.addAll(personalTasks.map { it.toHomeItem() })
        val homeAdapter =
            HomeAdapter(itemsList, ::onClickTask, ::onClickTask)
        binding.recyclerView.adapter = homeAdapter

    }

    private fun getTeamTasksCount(teamTasks: List<TeamTask>): List<Int> {
        val toDoTasksCount = teamTasks.count { it.statusTeamTask == TO_DO_STATUS }
        val inProgressTasksCount =
            teamTasks.count { it.statusTeamTask == IN_PROGRESS_STATUS }
        val doneTasksCount = teamTasks.count { it.statusTeamTask == DONE_STATUS }
        return listOf(toDoTasksCount, inProgressTasksCount, doneTasksCount)
    }

    private fun getPersonalTasksCount(teamTasks: List<PersonalTask>): List<Int> {
        val toDoTasksCount = teamTasks.count { it.statusPersonalTask == TO_DO_STATUS }
        val inProgressTasksCount =
            teamTasks.count { it.statusPersonalTask == IN_PROGRESS_STATUS }
        val doneTasksCount = teamTasks.count { it.statusPersonalTask == DONE_STATUS }
        return listOf(toDoTasksCount, inProgressTasksCount, doneTasksCount)
    }

    private fun onClickTask(id: String) {
        createToast(id)
    }

    private fun createToast(message: String?) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val TEAM_POSITION = 0
        const val PERSONAL_POSITION = 1
        const val TO_DO_STATUS = 0
        const val IN_PROGRESS_STATUS = 1
        const val DONE_STATUS = 2

    }
}