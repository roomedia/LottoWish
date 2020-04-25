package com.Roo_Media_.lottowish.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.ui.gamelist.GamelistFragment
import com.Roo_Media_.lottowish.ui.wishlist.WishlistFragment
import com.Roo_Media_.lottowish.utils.Crawl
import kotlinx.android.synthetic.main.fragment_lotto.*
import kotlinx.android.synthetic.main.fragment_lotto.view.*
import java.text.SimpleDateFormat
import java.util.*

class LottoFragment : Fragment() {

    private val now = Date()
    private val WEEK = 604800000
    private val balls: List<TextView> by lazy {
        lottoConstraint.children.toList().filterIsInstance<TextView>().subList(0, 8)
    }

    val crawl: Crawl by lazy { Crawl(context!!) }
    var currentPrize: Long? = null
    var nums = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_lotto, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getRound()
    }

    private fun getRound() {
        val dataAdapter = ArrayAdapter(
            context!!,
            R.layout.support_simple_spinner_dropdown_item,
            getRoundList()
        )

        round.adapter = dataAdapter
        round.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                crawl.getRound(parent!!.adapter.count, position) {
                    setRound(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        setAdapterPosition()
    }

    fun getRoundList(): List<String> {
        val startDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).parse("2002-12-07 20:50")!!

        val currentRound = ((now.time - startDate.time) / WEEK).toInt()
        val rounds = (currentRound downTo 0).map { "${it + 1}${getString(R.string.round)}" }

        return listOf(getString(R.string.round_next)) + rounds
    }

    fun setAdapterPosition() {
        val NOW = now.time % WEEK
        val SAT = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 50)
        }.run {
            this.timeInMillis % WEEK
        }

        val SUN = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
        }.run {
            this.timeInMillis % WEEK
        }

        if ((NOW - SAT > 0).and(NOW - SUN < 0)) {
            view!!.round.setSelection(1)
        }
    }

    fun setRound(params: List<Any>) {

        balls.forEach { it.isVisible = (round.selectedItemPosition != 0) }

        if (round.selectedItemPosition == 0) {
            (prize.layoutParams as ConstraintLayout.LayoutParams).apply {
                topToBottom = round.id
                topMargin = resources.getDimensionPixelSize(R.dimen.size_half)
                prize.requestLayout()
            }
        } else {
            (prize.layoutParams as ConstraintLayout.LayoutParams).apply {
                topToBottom = num1.id
                topMargin = resources.getDimensionPixelSize(R.dimen.size_single)
                prize.requestLayout()
            }

            nums.clear()
            nums.addAll(params[2] as Collection<String>)
            val balls =
                lottoConstraint.children.toList().filterIsInstance<TextView>()
                    .subList(0, nums.size)

            for ((ball: TextView, num: String) in balls zip nums) {
                setBall(ball, num)
            }

            plus.text = "+"
        }

        prize.text = params[0] as String
        currentPrize = params[1] as Long

        (context as MainActivity).getFragment().also {
            if (it is WishlistFragment) {
                it.setBalance()
            } else if (it is GamelistFragment) {
                it.getResult()
            }
        }
    }

    private fun setBall(view: TextView, num: String) {
        view.text = num
        view.setBackgroundResource(
            getResourceId(num)
        )
    }

    private fun getResourceId(num: String): Int {
        val packageName = context!!.packageName
        val resContext = context!!.createPackageContext(packageName, 0)
        val res = resContext.resources
        return res.getIdentifier("clr" + ((num.toInt() - 1) / 10 + 1), "drawable", packageName)
    }
}