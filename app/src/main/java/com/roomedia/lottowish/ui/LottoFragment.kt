package com.roomedia.lottowish.ui

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
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.FragmentLottoBinding
import com.roomedia.lottowish.ui.gamelist.GamelistFragment
import com.roomedia.lottowish.ui.wishlist.WishlistFragment
import com.roomedia.lottowish.utils.Crawl
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LottoFragment : Fragment() {
    private val now = Date()
    private val week = 604800000
    var binding: FragmentLottoBinding? = null
    private val balls: List<TextView> by lazy {
        binding
            ?.lottoConstraint
            ?.children
            ?.toList()
            ?.filterIsInstance<TextView>()
            ?.subList(0, 8)
            ?: emptyList()
    }

    val crawl: Crawl by lazy { Crawl(requireContext()) }
    var currentPrize: Long? = null
    var nums = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLottoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getRound()
    }

    private fun getRound() {
        val dataAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getRoundList(),
            )

        binding?.round?.adapter = dataAdapter
        binding?.round?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    crawl.getRound(parent?.adapter?.count ?: 0, position) {
                        setRound(it)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        setAdapterPosition()
    }

    private fun getRoundList(): List<String> {
        val startDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).parse("2002-12-07 20:50")!!

        val currentRound = ((now.time - startDate.time) / week).toInt()
        val rounds = (currentRound downTo 0).map { "${it + 1}${getString(R.string.round)}" }

        return listOf(getString(R.string.round_next)) + rounds
    }

    private fun setAdapterPosition() {
        val now = now.time % week
        val sat =
            Calendar
                .getInstance()
                .apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 50)
                }.run {
                    this.timeInMillis % week
                }

        val sun =
            Calendar
                .getInstance()
                .apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 0)
                }.run {
                    this.timeInMillis % week
                }

        if ((now - sat > 0).and(now - sun < 0)) {
            binding?.round?.setSelection(1)
        }
    }

    fun setRound(params: List<Any>) {
        balls.forEach { it.isVisible = (binding?.round?.selectedItemPosition != 0) }

        if (binding?.round?.selectedItemPosition == 0) {
            (binding?.prize?.layoutParams as ConstraintLayout.LayoutParams).apply {
                topToBottom = binding?.round?.id!!
                topMargin = resources.getDimensionPixelSize(R.dimen.size_half)
                binding?.prize?.requestLayout()
            }
        } else {
            (binding?.prize?.layoutParams as ConstraintLayout.LayoutParams).apply {
                topToBottom = binding?.num1?.id!!
                topMargin = resources.getDimensionPixelSize(R.dimen.size_single)
                binding?.prize?.requestLayout()
            }

            nums.clear()
            nums.addAll(params[2] as Collection<String>)
            val balls =
                binding
                    ?.lottoConstraint
                    ?.children
                    ?.toList()
                    ?.filterIsInstance<TextView>()
                    ?.subList(0, nums.size)
                    ?: emptyList()

            for ((ball: TextView, num: String) in balls zip nums) {
                setBall(ball, num)
            }

            binding?.plus?.text = "+"
        }

        binding?.prize?.text = params[0] as String
        currentPrize = params[1] as Long

        (context as MainActivity).getFragment().also {
            if (it is WishlistFragment) {
                it.setBalance()
            } else if (it is GamelistFragment) {
                it.getResult()
            }
        }
    }

    private fun setBall(
        view: TextView,
        num: String,
    ) {
        view.text = num
        view.setBackgroundResource(
            getResourceId(num),
        )
    }

    private fun getResourceId(num: String): Int {
        val packageName = requireContext().packageName
        val resContext = requireContext().createPackageContext(packageName, 0)
        val res = resContext.resources
        return res.getIdentifier("clr" + ((num.toInt() - 1) / 10 + 1), "drawable", packageName)
    }
}
