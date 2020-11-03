package kg.jobs.app.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import kg.jobs.app.R

class LoadingDialog(context: Context) : AlertDialog(context) {
    init {
        val view = LayoutInflater.from(context)?.inflate(R.layout.dialog_loading, null)!!
        setCancelable(false)
        setView(view)
        window?.decorView?.setBackgroundResource(android.R.color.transparent)
    }
}