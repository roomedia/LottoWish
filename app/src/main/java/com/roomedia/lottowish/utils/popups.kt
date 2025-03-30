package com.roomedia.lottowish.utils

import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import com.roomedia.lottowish.R

fun <T : Any> deletePopup(
    target: MutableLiveData<T>,
    source: T,
    deleteAll: MutableLiveData<Boolean>,
    view: View
) {
    fun listener(
        target: MutableLiveData<T>,
        source: T,
        deleteAll: MutableLiveData<Boolean>,
        menu: MenuItem,
        view: View
    ): Boolean {
        return when (menu.itemId) {
            R.id.delete -> {
                target.value = source
                true
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
                true
            }

            else -> false
        }
    }

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
