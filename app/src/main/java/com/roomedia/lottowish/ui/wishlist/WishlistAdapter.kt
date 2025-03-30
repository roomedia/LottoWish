package com.roomedia.lottowish.ui.wishlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.ItemWishlistBinding
import com.roomedia.lottowish.room.wishlist.Wish
import com.roomedia.lottowish.utils.deletePopup
import com.roomedia.lottowish.utils.makeBriefCost

class WishlistAdapter(val context: Context, val wishlist: ArrayList<Wish>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val updateWish = MutableLiveData<Wish>()
    val deleteWish = MutableLiveData<Wish>()
    val deleteAll = MutableLiveData<Boolean>()
    private var balance: Long = 0
    private var limit = MutableLiveData<Int>()

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
        val binding = ItemWishlistBinding.bind(holder.itemView)
        holder.itemView.also {
            if (position < (limit.value ?: 0)) {
                binding.priority.text = ""
                binding.priority.setBackgroundResource(R.drawable.ic_check)
            } else {
                binding.priority.text = (position + 1).toString()
                binding.priority.setBackgroundResource(0)
            }

            binding.title.text = wish.title
            binding.cost.text =
                makeBriefCost(context, wish.cost)
        }
            .also {
                it.setOnClickListener {
                    updateWish.value = wish
                }
                binding.option.setOnClickListener {
                    deletePopup(
                        deleteWish,
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
