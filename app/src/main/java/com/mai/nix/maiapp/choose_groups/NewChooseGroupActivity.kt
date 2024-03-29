package com.mai.nix.maiapp.choose_groups

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mai.nix.maiapp.ActivityChooseSingleItem
import com.mai.nix.maiapp.MainActivity
import com.mai.nix.maiapp.R
import com.mai.nix.maiapp.MVIEntity
import com.mai.nix.maiapp.helpers.UserSettings
import kotlinx.android.synthetic.main.activity_new_choose_group.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NewChooseGroupActivity : AppCompatActivity(),
        GroupsAdapter.GroupChosenListener,
        View.OnClickListener,
        MVIEntity {

    companion object {
        const val FACULTIES_RESULT_CODE = 23
        const val COURSES_RESULT_CODE = 24

        const val EXTRA_GROUP = "com.mai.nix.maiapp.choose_groups.group_result"
        private const val MODE = "com.mai.nix.maiapp.choose_groups.maiapp.mode"

        fun newIntent(context: Context, mode: Boolean): Intent {
            val intent = Intent(context, NewChooseGroupActivity::class.java)
            intent.putExtra(MODE, mode)
            return intent
        }
    }

    private lateinit var groupsViewModel: GroupsViewModel

    private val faculties = arrayOf(
            "Институт №1",
            "Институт №2",
            "Институт №3",
            "Институт №4",
            "Институт №5",
            "Институт №6",
            "Институт №7",
            "Институт №8",
            "Институт №9",
            "Институт №10",
            "Институт №11",
            "Институт №12"
    )

    private val courses = arrayOf(
            "1 курс",
            "2 курс",
            "3 курс",
            "4 курс",
            "5 курс",
            "6 курс"
    )

    private val groupsAdapter = GroupsAdapter()
    private var isForSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_choose_group)
        groupsAdapter.callback = this
        isForSettings = intent.getBooleanExtra(MODE, false)
        prepareRecyclerView()
        setupViewModel()
        observeViewModel()

        chooseGroupSRL.setOnRefreshListener {
            refresh()
        }

        chooseFacultyButton.setOnClickListener(this)
        chooseCourseButton.setOnClickListener(this)
        readyButton.setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.chooseFacultyButton -> {
                ActivityChooseSingleItem.startActivity(this, faculties, FACULTIES_RESULT_CODE)
            }

            R.id.chooseCourseButton -> {
                ActivityChooseSingleItem.startActivity(this, courses, COURSES_RESULT_CODE)
            }

            R.id.readyButton -> {
                if (!isForSettings) {
                    UserSettings.setGroup(this, groupsViewModel.state.value.chosenGroup)
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    setGroupResult(groupsViewModel.state.value.chosenGroup)
                    finish()
                }
            }
        }
    }

    private fun setGroupResult(group: String) {
        val data = Intent()
        data.putExtra(EXTRA_GROUP, group)
        setResult(RESULT_OK, data)
    }

    override fun setupViewModel() {
        groupsViewModel = ViewModelProviders.of(this, GroupsViewModelFactory()).get(GroupsViewModel::class.java)
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            groupsViewModel.state.collect {
                chooseFacultyButton.text = if (it.faculty.isNotEmpty()) it.faculty else getString(R.string.choose_faculty_space)
                chooseCourseButton.text = if (it.course.isNotEmpty()) it.course else getString(R.string.choose_course_space)
                chooseGroupSRL.isRefreshing = it.loading
                groupsAdapter.setItems(it.groups, it.index)
                readyButton.visibility = if (it.chosenGroup.isEmpty()) View.GONE else View.VISIBLE
                if (it.error != null) {
                    Toast.makeText(this@NewChooseGroupActivity, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onGroupChosen(group: String, index: Int) {
        setGroup(group, index)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val chosenIndex = data?.getIntExtra(ActivityChooseSingleItem.ITEMS_RESULT, 0) ?: 0
            if (requestCode == FACULTIES_RESULT_CODE) {
                lifecycleScope.launch {
                    groupsViewModel.intent.send(GroupsIntent.SetFaculty(faculties[chosenIndex]))
                }
            } else if (requestCode == COURSES_RESULT_CODE) {
                lifecycleScope.launch {
                    groupsViewModel.intent.send(GroupsIntent.SetCourse(courses[chosenIndex]))
                }
            }
        }
    }

    private fun prepareRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        groupsRecyclerView.layoutManager = linearLayoutManager
        groupsRecyclerView.adapter = groupsAdapter
        val dividerItemDecoration = DividerItemDecoration(this, linearLayoutManager.orientation)
        groupsRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun refresh() {
        lifecycleScope.launch {
            groupsViewModel.intent.send(GroupsIntent.UpdateGroups)
        }
    }

    private fun setGroup(group: String, index: Int) {
        lifecycleScope.launch {
            groupsViewModel.intent.send(GroupsIntent.SetChosenGroup(group, index))
        }
    }
}