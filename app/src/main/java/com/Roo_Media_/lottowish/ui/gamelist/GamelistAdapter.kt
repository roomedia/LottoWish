package com.Roo_Media_.lottowish.ui.gamelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.gamelist.Game
import com.Roo_Media_.lottowish.utils.deletePopup
import kotlinx.android.synthetic.main.item_gamelist.view.*

class GamelistAdapter(val gamelist: List<Game>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val updateGame = MutableLiveData<Game>()
    val deleteGame = MutableLiveData<Game>()
    val deleteAll = MutableLiveData<Boolean>()
    val lottoNum = mutableListOf<String>()
    val resultList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_gamelist, parent, false)
        )

    override fun getItemCount(): Int = gamelist.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val game = gamelist[position]

        holder.itemView.apply {
            val nums = game.number.split(" ")
            val balls = lottoConstraint.children.toList().filterIsInstance<TextView>()
            for ((ball, num) in balls zip nums) {
                if (num in lottoNum) {
                    ball.setBackgroundResource(
                        resources.getIdentifier(
                            "clr" + ((num.toInt() - 1) / 10 + 1),
                            "drawable",
                            context!!.packageName
                        )
                    )
                    ball.setTextColor(
                        resources.getColor(
                            android.R.color.background_light
                        )
                    )
                } else {
                    ball.setBackgroundResource(R.drawable.clr0)
                    ball.setTextColor(itemResult.currentTextColor)
                }
                ball.text = num
            }

            itemResult.text =
                if (position < resultList.size) {
                    resultList[position]
                } else {
                    ""
                }

            this.setOnClickListener {
                updateGame.value = game
            }

            option.setOnClickListener {
                deletePopup(
                    deleteGame as MutableLiveData<Any>,
                    game,
                    deleteAll,
                    it
                )
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}