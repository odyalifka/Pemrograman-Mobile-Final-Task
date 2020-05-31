package com.unhas.todolist.ui.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.unhas.todolist.R
import kotlinx.android.synthetic.main.dialog_confirm.*


class ConfirmDialog(context: Context, private val title: String, private val message: String, private val isCancleable: Boolean = true, private var btnPosText: String = "Yes", private var btnNegText: String = "No", private val yesAction: () -> Unit): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.dialog_confirm)

        setCanceledOnTouchOutside(isCancleable)

        tv_dialog_title.text = title
        tv_dialog_msg.text = message
        btn_no.text = btnNegText
        btn_yes.text = btnPosText
        btn_no.setOnClickListener{dismiss()}
        btn_yes.setOnClickListener {
            yesAction()
            dismiss()
        }
    }
}