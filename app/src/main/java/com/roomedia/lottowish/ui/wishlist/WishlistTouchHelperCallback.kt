package com.roomedia.lottowish.ui.wishlist

import android.graphics.Canvas
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class WishlistTouchHelperCallback(
    private val viewModel: WishlistViewModel,
    dragDirs: Int,
    swipeDirs: Int
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    private var isPastActive: Boolean = false

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        (recyclerView.adapter as WishlistAdapter).apply {
            swapItems(source.adapterPosition, target.adapterPosition)
            setBalance()
        }
        return true
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (isPastActive.and(!isCurrentlyActive)) {
            Log.d("over", "released!!")
            (recyclerView.adapter as WishlistAdapter).wishlist.forEach {
                viewModel.update(it)
            }
        }

        isPastActive = isCurrentlyActive
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return 0
    }
}
