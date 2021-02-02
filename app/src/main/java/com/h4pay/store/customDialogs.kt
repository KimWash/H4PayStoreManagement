package com.h4pay.store

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object customDialogs {
    fun yesNoDialog(context: Context, title: String, message:String, yesEvent: () -> Unit, noEvent:() -> Unit){
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        yesEvent()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        noEvent()
                    }
                }
            }


        AlertDialog.Builder(
            context,
            R.style.AlertDialogTheme
        ).apply{
            this.setTitle("확인")
            this.setMessage("정말로 취소처리 하시겠습니까?")
            this.setPositiveButton("예", dialogClickListener)
            this.setNegativeButton("아니오", dialogClickListener)
            this.show()
        }
    }

    fun yesOnlyDialog(context:Context, msg:String, okEvent: () -> Unit, title:String, icon:Int?){
        val alert_confirm = AlertDialog.Builder(context)
        alert_confirm.setMessage(msg)
        alert_confirm.setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
            okEvent()
        })
        if (icon != null){
            alert_confirm
                .setTitle(title)
                .create()
                .setIcon(icon)
        }
        else{
            alert_confirm
                .setTitle(title)
                .create()
        }
        alert_confirm.show()
    }
}