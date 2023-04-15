package com.chocolatecake.todoapp.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.widget.addTextChangedListener
import com.chocolatecake.todoapp.R
import com.chocolatecake.todoapp.databinding.FragmentHomeBinding
import com.chocolatecake.todoapp.add_new_task.view.AddNewTaskFragment
import com.chocolatecake.todoapp.base.fragment.BaseFragment
import com.chocolatecake.todoapp.core.data.model.response.PersonalTask
import com.chocolatecake.todoapp.core.data.model.response.TeamTask
import com.chocolatecake.todoapp.home.adapter.HomeAdapter
import com.chocolatecake.todoapp.home.model.HomeItem
import com.chocolatecake.todoapp.home.model.SearchQuery
import com.chocolatecake.todoapp.home.model.Status
import com.chocolatecake.todoapp.home.presenter.HomePresenter
import com.chocolatecake.todoapp.home.utils.toHomeItem
import com.chocolatecake.todoapp.login.LoginFragment
import com.chocolatecake.todoapp.task_details.view.TaskDetailsFragment
import com.chocolatecake.todoapp.core.util.hide
import com.chocolatecake.todoapp.core.util.navigateExclusive
import com.chocolatecake.todoapp.core.util.navigateTo
import com.chocolatecake.todoapp.core.util.show
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout

class HomeFragment : BaseFragment<FragmentHomeBinding>(), HomeView {
    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    private val presenter by lazy { HomePresenter(this, requireContext()) }
    private val homeAdapter: HomeAdapter by lazy {
        val itemsList: MutableList<HomeItem> = mutableListOf()
        HomeAdapter(itemsList, ::onClickTeamTask, ::onClickPersonalTask).also {
            binding.recyclerView.adapter = it
        }
    }
    private var searchQuery: SearchQuery = SearchQuery()
    private var isPersonal = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        addCallBacks()
    }

    private fun setup() {
        presenter.getTeamTask(getSelectedChips())
        presenter.getTeamStatusListCount()
    }

    private fun addCallBacks() {
        binding.floatingActionButton.setOnClickListener {
            val addNewTaskFragment = AddNewTaskFragment.newInstance(isPersonal)
            requireActivity().navigateTo(addNewTaskFragment)
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.apply {
                    when (tab.position) {
                        TEAM_POSITION -> {
                            presenter.getTeamStatusListCount()
                            presenter.searchTeamTasks(searchQuery)
                            isPersonal = false
                        }
                        PERSONAL_POSITION -> {
                            presenter.getPersonalStatusListCount()
                            presenter.searchPersonalTasks(searchQuery)
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
        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            searchQuery = searchQuery.copy(status = getSelectedChips())
            if (isPersonal) {
                presenter.searchPersonalTasks(searchQuery)
                presenter.getPersonalStatusListCount()
            } else {
                presenter.searchTeamTasks(searchQuery)
                presenter.getTeamStatusListCount()
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

    override fun onAllTasksFailure(message: String?) {
        runOnUi { showNoNetworkError() }
    }

    private fun showNoTasksError() {
        binding.recyclerView.hide()
        binding.imageViewNoTasksResult.show()
        binding.textViewNoTasksResult.show()
    }

    private fun showNoNetworkError() {
        binding.recyclerView.hide()
        binding.imageViewNoTasksResult.hide()
        binding.lottieNoNetwork.show()
        binding.textViewNoNetwork.show()

    }

    private fun showRecyclerView() {
        binding.groupNoNetwork.hide()
        binding.recyclerView.show()
    }


    override fun onTeamTasksSuccess(teamTasks: List<TeamTask>) {
        runOnUi {
            showRecyclerView()
            if (teamTasks.isEmpty()){
                showNoTasksError()
            }
            setUpTeamTasksRecyclerView(teamTasks)
        }
    }

    override fun onPersonalTasksSuccess(personalTasks: List<PersonalTask>) {
        runOnUi {
            showRecyclerView()
            if (personalTasks.isEmpty()){
                showNoTasksError()
            }
            setUpPersonalTasksRecyclerView(personalTasks)
        }
    }

    override fun onUnauthorizedResponse() {
        requireActivity().navigateExclusive(LoginFragment())
    }

    override fun onSearchTeamResultSuccess(teamTasks: List<TeamTask>) {
        runOnUi {
            showRecyclerView()
            if (teamTasks.isEmpty()){
                showNoTasksError()
            }
            val itemsList: MutableList<HomeItem> = mutableListOf()
            itemsList.addAll(teamTasks.map { it.toHomeItem() })
            homeAdapter.updateList(itemsList)
        }
    }

    override fun onSearchPersonalResultSuccess(personalTasks: List<PersonalTask>) {
        val itemsList: MutableList<HomeItem> = mutableListOf()
        itemsList.addAll(personalTasks.map { it.toHomeItem() })
        runOnUi {
            showRecyclerView()
            if (personalTasks.isEmpty()){
                showNoTasksError()
            }
            homeAdapter.updateList(itemsList)
        }
    }

    override fun onStatusCountsSuccess(statusList: Triple<Int?, Int?, Int?>) {
        runOnUi {
            showRecyclerView()
            updateChipsStatus(statusList)
        }
    }

    private fun updateChipsStatus(tasksCount: Triple<Int?, Int?, Int?>) {
        with(binding) {
            chipGroup.children.forEach {
                when (it.id) {
                    R.id.toDoChip -> {
                        toDoChip.text = getString(R.string.to_do_task, tasksCount.first)
                    }
                    R.id.InProgressChip -> {
                        InProgressChip.text =
                            getString(R.string.in_progress_task, tasksCount.second)
                    }
                    R.id.DoneChip -> {
                        DoneChip.text = getString(R.string.done_task, tasksCount.third)
                    }
                }
            }
        }
    }

    private fun setUpTeamTasksRecyclerView(teamTasks: List<TeamTask>) {
        val itemsList: MutableList<HomeItem> = mutableListOf()
        itemsList.addAll(teamTasks.map { it.toHomeItem() })
        homeAdapter.updateList(itemsList)
    }


    private fun setUpPersonalTasksRecyclerView(personalTasks: List<PersonalTask>) {
        val itemsList: MutableList<HomeItem> = mutableListOf()
        itemsList.addAll(personalTasks.map { it.toHomeItem() })
        homeAdapter.updateList(itemsList)
    }


    private fun onClickTeamTask(teamTask: TeamTask) {
        val taskDetailsFragment = TaskDetailsFragment.newTeamInstance(teamTask)
        activity?.navigateTo(taskDetailsFragment)
    }

    private fun onClickPersonalTask(personalTask: PersonalTask) {
        val taskDetailsFragment = TaskDetailsFragment.newPersonalInstance(personalTask)
        activity?.navigateTo(taskDetailsFragment)
    }

    private fun runOnUi(runner: () -> Unit) = activity?.runOnUiThread { runner() }

    private companion object {
        const val TEAM_POSITION = 0
        const val PERSONAL_POSITION = 1
    }
}