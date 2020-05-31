package com.unhas.todolist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.unhas.todolist.MainActivity
import com.unhas.todolist.R
import com.unhas.todolist.db.Task
import com.unhas.todolist.ui.util.Commons
import kotlinx.android.synthetic.main.item_empty.view.*
import kotlinx.android.synthetic.main.item_task.view.*
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private val listener: (Task, Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_TASK = 1
    private var taskList = listOf<Task>()
    private var taskFilteredList = listOf<Task>()
    fun setTaskList(taskList: List<Task>){
        this.taskList = taskList
        taskFilteredList = taskList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keywords = constraint.toString()
                if (keywords.isEmpty())
                    taskFilteredList = taskList
                else{
                    val filteredList = ArrayList<Task>()
                    for (task in taskList) {
                        if (task.toString().toLowerCase(Locale.ROOT).contains(keywords.toLowerCase(Locale.ROOT)))
                            filteredList.add(task)
                    }
                    taskFilteredList = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = taskFilteredList
                return  filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                taskFilteredList = results?.values as List<Task>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (taskFilteredList.isEmpty())
            VIEW_TYPE_EMPTY
        else
            VIEW_TYPE_TASK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        return when (viewType) {
            VIEW_TYPE_TASK -> TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))
            VIEW_TYPE_EMPTY -> EmptyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false))
            else -> throw throw IllegalArgumentException("Undefined view type")
        }
    }


    override fun getItemCount(): Int = if (taskFilteredList.isEmpty()) 1 else taskFilteredList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_EMPTY -> {
                val emptyHolder = holder as EmptyViewHolder
                emptyHolder.bindItem()
            }
            VIEW_TYPE_TASK -> {
                val taskHolder = holder as TaskViewHolder
                val sortedList = taskFilteredList.sortedWith(
                    if(MainActivity.isSortByDateCreated)
                        compareBy({it.dateCreated}, {it.dateUpdated})
                    else{
                        compareBy({it.dueDate}, {it.dueTime})
                    })
                taskHolder.bindItem(sortedList[position], listener)
            }
        }
    }

    class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItem(task: Task, listener: (Task, Int) -> Unit){
            val parsedDateCreated = SimpleDateFormat("dd/MM/yy", Locale.US).parse(task.dateCreated) as Date
            val dateCreated = Commons.formatDate(parsedDateCreated, "dd MMM yyyy")

            val parsedDateUpdated = SimpleDateFormat("dd/MM/yy", Locale.US).parse(task.dateCreated) as Date
            val dateUpdated = Commons.formatDate(parsedDateUpdated, "dd MMM yyyy")

            val date = if (task.dateUpdated != task.dateCreated) "Updated at $dateUpdated" else "Created at $dateCreated"

            val parsedDate = SimpleDateFormat("dd/MM/yy", Locale.US).parse(task.dueDate) as Date
            val dueDate = Commons.formatDate(parsedDate, "dd MMM yyyy")

            val dueDateTime = "Due ${dueDate}, ${task.dueTime}"

            itemView.tv_title.text = task.title
            itemView.tv_detail.text = task.note
            itemView.tv_due_date.text = dueDateTime
            itemView.tv_created_date.text = date

            itemView.setOnClickListener{
                listener(task, layoutPosition)
            }
        }
    }

    class EmptyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItem(){
            itemView.tv_empty.text = "No data found"
        }
    }
}