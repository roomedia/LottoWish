package com.roomedia.lottowish.utils

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class SmoothLayout(
    val context: Context,
) : LinearLayoutManager(context) {
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int,
    ) {
        val dy = position - findFirstVisibleItemPosition()
        val viewLimit = 35
        val itemLimit = 1

        if (dy > viewLimit) {
            recyclerView.scrollToPosition(position - viewLimit)
        }
        if (dy >= itemLimit) {
            val linearSmoothScroller = SmoothScroller(context, min(dy, viewLimit))
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }
    }

    inner class SmoothScroller(
        context: Context,
        private val dy: Int,
    ) : LinearSmoothScroller(context) {
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float = (1.5 / dy).toFloat()
    }
}
