package com.Roo_Media_.lottowish.utils

import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import com.Roo_Media_.lottowish.R

fun deletePopup(
    target: MutableLiveData<Any>,
    source: Any,
    deleteAll: MutableLiveData<Boolean>,
    view: View
) {
    PopupMenu(view.context, view)
        .apply {
            inflate(R.menu.wish_menu)
        }
        .apply {
            setOnMenuItemClickListener {
                listener(
                    target,
                    source,
                    deleteAll,
                    it,
                    view
                )
            }
        }.show()
}

private fun listener(
    target: MutableLiveData<Any>,
    source: Any,
    deleteAll: MutableLiveData<Boolean>,
    menu: MenuItem,
    view: View
): Boolean {

    when (menu.itemId) {
        R.id.delete -> {
            target.value = source
            return true
        }
        R.id.delete_all -> {
            AlertDialog.Builder(view.context)
                .setTitle(R.string.delete_all_title)
                .setMessage(R.string.delete_all_message)

                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }

                .setPositiveButton(R.string.delete) { dialog, _ ->
                    deleteAll.value = true
                    dialog.dismiss()
                }
                .create().show()
            return true
        }
        else -> return false
    }
}