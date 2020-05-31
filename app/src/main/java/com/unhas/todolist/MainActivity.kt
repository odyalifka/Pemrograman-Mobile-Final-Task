package com.unhas.todolist

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.unhas.todolist.db.Task
import com.unhas.todolist.ui.TaskAdapter
import com.unhas.todolist.ui.TaskViewModel
import com.unhas.todolist.ui.util.Commons
import com.unhas.todolist.ui.util.ConfirmDialog
import com.unhas.todolist.ui.util.FormDialog
import com.unhas.todolist.ui.util.NotifyAlarm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.task_fragment.view.*

class MainActivity : AppCompatActivity() {
    companion object{
        var isSortByDateCreated = true
    }
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var notifyAlarm: NotifyAlarm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        rv.layoutManager = layoutManager

        taskAdapter = TaskAdapter() { task, _ ->
            val options = resources.getStringArray(R.array.option_item)
            Commons.showSelector(this, "Choose Action", options) { _, i ->
                when (i) {
                    0 -> showDetailDialog(task)
                    1 -> showUpdateDialog(task)
                    2 -> showDeleteDialog(task)
                }
            }
        }

        rv.adapter = taskAdapter
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        swipe_refresh_layout.setOnRefreshListener {
            refreshData()
        }

        button_add.setOnClickListener{
            showInsertDialog()
        }

        notifyAlarm = NotifyAlarm()
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        taskViewModel.getTasks()?.observe(this, Observer {
            taskAdapter.setTaskList(it)
            setProgressbarVisibility(false)
        })

    }

    private fun setEmptyTextVisibility(state: Boolean) {
        if (state) tv_empty.visibility = View.VISIBLE
        else tv_empty.visibility = View.GONE
    }

    private fun setProgressbarVisibility(state: Boolean) {
        if (state) progressbar.visibility = View.VISIBLE
        else progressbar.visibility = View.INVISIBLE
    }

    private fun refreshData() {
        setProgressbarVisibility(true)
        observeData()
        swipe_refresh_layout.isRefreshing = false
        setProgressbarVisibility(false)
    }

    private fun showDeleteDialog(task: Task) {
        val dialogTitle = "Delete"
        val dialogMessage = "Are you sure want to delete this task?"
        val toastMessage = "Data has been deleted successfully"

        ConfirmDialog(this, dialogTitle, dialogMessage) {
            taskViewModel.deleteTask(task)
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showUpdateDialog(task: Task) {
        val view = LayoutInflater.from(this).inflate(R.layout.task_fragment,null)
        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        view.input_title.setText(task.title)
        view.input_detail_task.setText(task.note)
        view.input_due_date.setText(task.dueDate)
        view.input_time.setText(task.dueTime)
        view.input_remind_me.isChecked = task.remindMe

        val dialogTitle = "Edit Task"
        val toastMessage = "Task has been updated successfully"
        val failAlertMessage = "Please fill all the required fields"

        FormDialog(this, dialogTitle, view){
            val title = view.input_title.text.toString().trim()
            val note = view.input_detail_task.text.toString()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()

            val dateCreated = task.dateCreated
            val remindMe = view.input_remind_me.isChecked
            val prevDueTime = task.dueTime

            if (title == "" || date == "" || time == "") {
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            }else{
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateUpdated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                task.title = title
                task.note = note
                task.dateCreated = dateCreated
                task.dateUpdated = dateUpdated
                task.dueDate = dueDate
                task.dueTime = time
                task.remindMe = remindMe

                taskViewModel.updateTask(task)
                if (remindMe && prevDueTime!=time) {
                    notifyAlarm.setReminderAlarm(this, dueDate, time,"$title is due in 1 hour")
                }
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showDetailDialog(task: Task) {
        val title = "Title: ${task.title}"
        val dueDate = "Due date: ${task.dueDate}, ${task.dueTime}"
        val note = "Note: ${task.note}"
        val dateCreated = "Date created: ${task.dateCreated}"
        val dateUpdated = "Date updated: ${task.dateUpdated}"
        val strReminder = if(task.remindMe) "Enabled" else "Disabled"
        val remindMe = "Reminder: $strReminder"

        val strMessage = "$title\n$dueDate\n$note\n\n$dateCreated\n$dateUpdated\n$remindMe"

        AlertDialog.Builder(this).setMessage(strMessage).setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    private fun showInsertDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.task_fragment,null)

        view.input_due_date.setOnClickListener{
            Commons.showDatePickerDialog(this, view.input_due_date)
        }
        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        val dialogTitle = "Add Task"
        val toastMessage = "Task has been added successfully"
        val failAlertMessage = "Please fill all required fields"

        FormDialog(this,dialogTitle,view){
            val title = view.input_title.text.toString().trim()
            val note = view.input_detail_task.text.toString()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()

            val remindMe = view.input_remind_me.isChecked

            if(title == "" || date == "" || time == ""){
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            }else{
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateCreated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                val task = Task(
                    title = title,
                    note = note,
                    dateCreated = dateCreated,
                    dateUpdated = dateCreated,
                    dueDate = dueDate,
                    dueTime = time,
                    remindMe = remindMe
                )

                taskViewModel.insertTask(task)
                if (remindMe) {
                    notifyAlarm.setReminderAlarm(this, dueDate, time,"$title is due in 1 hour")
                }
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = (menu.findItem(R.id.search)).actionView as androidx.appcompat.widget.SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Search tasks"
        searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                taskAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskAdapter.filter.filter(newText)
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort -> true
            R.id.sort_by_date_created -> {
                isSortByDateCreated = true
                refreshData()
                true
            }
            R.id.sort_by_due_date -> {
                isSortByDateCreated = false
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
