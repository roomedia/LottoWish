package com.roomedia.lottowish.ui.gamelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.ItemGamelistBinding
import com.roomedia.lottowish.room.gamelist.Game
import com.roomedia.lottowish.utils.deletePopup

class GamelistAdapter(val gamelist: List<Game>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val updateGame = MutableLiveData<Game>()
    val deleteGame = MutableLiveData<Game>()
    val deleteAll = MutableLiveData<Boolean>()
    val lottoNum = mutableListOf<String>()
    val resultList = mutableListOf<String>()
    private lateinit var binding: ItemGamelistBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ItemGamelistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int = gamelist.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val game = gamelist[position]

        holder.itemView.apply {
            val nums = game.number.split(" ")
            val balls = binding.lottoConstraint.children.toList().filterIsInstance<TextView>()
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
                        ContextCompat.getColor(
                            context,
                            android.R.color.background_light
                        )
                    )
                } else {
                    ball.setBackgroundResource(R.drawable.clr0)
                    ball.setTextColor(binding.itemResult.currentTextColor)
                }
                ball.text = num
            }

            binding.itemResult.text =
                if (position < resultList.size) {
                    resultList[position]
                } else {
                    ""
                }

            this.setOnClickListener {
                updateGame.value = game
            }

            binding.option.setOnClickListener {
                deletePopup(
                    deleteGame,
                    game,
                    deleteAll,
                    it
                )
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
