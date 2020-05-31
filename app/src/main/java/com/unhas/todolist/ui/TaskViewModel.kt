package com.unhas.todolist.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.unhas.todolist.db.Task
import com.unhas.todolist.db.TaskRepository

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private var taskRepository =
        TaskRepository(application)
    private var tasks: LiveData<List<Task>>? = taskRepository.getTasks()

    fun getTasks(): LiveData<List<Task>>? {
        return tasks
    }

    fun insertTask(task: Task){
        taskRepository.insert(task)
    }

    fun deleteTask(task: Task){
        taskRepository.delete(task)
    }

    fun updateTask(task: Task){
        taskRepository.update(task)
    }
}
