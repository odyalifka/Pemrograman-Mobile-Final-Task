package com.unhas.todolist.ui.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.unhas.todolist.R
import kotlinx.android.synthetic.main.dialog_form.*

class FormDialog(context: Context, private val title: String, private val formLayout: View, private val saveAction: () -> Unit): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.dialog_form)

        if(formLayout.parent != null){
            (formLayout.parent as ViewGroup).removeView(formLayout)
        }

        tv_form_title.text = title
        form_container.addView(formLayout)
        btn_cancel.setOnClickListener{dismiss()}
        btn_save.setOnClickListener{
            saveAction()
            dismiss()
        }
    }
}