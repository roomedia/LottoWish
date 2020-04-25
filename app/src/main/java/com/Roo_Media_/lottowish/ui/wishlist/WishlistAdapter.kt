package com.Roo_Media_.lottowish.ui.wishlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.wishlist.Wish
import com.Roo_Media_.lottowish.utils.deletePopup
import com.Roo_Media_.lottowish.utils.makeBriefCost
import kotlinx.android.synthetic.main.item_wishlist.view.*

class WishlistAdapter(val context: Context, val wishlist: ArrayList<Wish>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val updateWish = MutableLiveData<Wish>()
    val deleteWish = MutableLiveData<Wish>()
    val deleteAll = MutableLiveData<Boolean>()
    var balance: Long = 0
    var limit = MutableLiveData<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_wishlist,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = wishlist.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val wish = wishlist[position]
        holder.itemView.also {
            if (position < (limit.value ?: 0)) {
                it.priority.text = ""
                it.priority.setBackgroundResource(R.drawable.ic_check)
            } else {
                it.priority.text = (position + 1).toString()
                it.priority.setBackgroundResource(0)
            }

            it.title.text = wish.title
            it.cost.text =
                makeBriefCost(context, wish.cost)
        }
            .also {
                it.setOnClickListener {
                    updateWish.value = wish
                }
                it.option.setOnClickListener {
                    deletePopup(
                        deleteWish as MutableLiveData<Any>,
                        wish,
                        deleteAll,
                        it
                    )
                }
            }
    }

    fun setBalance(total: Long? = null): Long {
        balance = total ?: balance
        var newBalance = balance
        var change = true

        wishlist.forEachIndexed { i, it ->
            newBalance -= it.cost
            if (newBalance < 0 && change) {
                limit.value = i
                change = false
            }
        }

        if (change)
            limit.value = wishlist.size

        return newBalance
    }

    fun swapItems(fromPosition: Int, toPosition: Int) {
        val tmp = wishlist[toPosition]
        wishlist[toPosition] = wishlist[fromPosition]
        wishlist[fromPosition] = tmp

        wishlist[toPosition].priority = toPosition
        wishlist[fromPosition].priority = fromPosition

        notifyItemMoved(fromPosition, toPosition)
        notifyItemChanged(fromPosition)
        notifyItemChanged(toPosition)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}