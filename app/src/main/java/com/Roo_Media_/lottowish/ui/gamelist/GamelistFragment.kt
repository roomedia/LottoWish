package com.Roo_Media_.lottowish.ui.gamelist

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.gamelist.Game
import com.Roo_Media_.lottowish.ui.LottoFragment
import com.Roo_Media_.lottowish.ui.MainActivity
import com.Roo_Media_.lottowish.utils.Preferences
import com.Roo_Media_.lottowish.utils.SmoothLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lotto.*
import kotlinx.android.synthetic.main.fragment_recycler.*
import kotlinx.android.synthetic.main.fragment_recycler.view.*

class GamelistFragment : Fragment() {

    private val gamelist = arrayListOf<Game>()
    private val gamelistViewModel: GamelistViewModel by lazy {
        ViewModelProviders.of(this)
            .get(GamelistViewModel::class.java)
    }

    private val preferences: Preferences by lazy { Preferences(activity!!) }
    private val PREVIEWTEXT: String by lazy {
        getString(R.string.preview)
            .replace("$", getString(R.string.preview_game))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_recycler, container, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = GamelistAdapter(gamelist)
        recycler.apply {
            this.adapter = adapter
            this.layoutManager =
                SmoothLayout(context)
            this.setHasFixedSize(true)
            this.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        adapter.updateGame.observe(
            this,
            Observer {
                makeBottomSheetFragment(it)
            }
        )

        adapter.deleteGame.observe(
            this,
            Observer {
                gamelistViewModel.delete(it)
            }
        )

        adapter.deleteAll.observe(
            this,
            Observer {
                gamelistViewModel.deleteAll()
            }
        )

        gamelistViewModel.getAll().observe(this, Observer {
            gamelist.clear()
            gamelist.addAll(it!!)

            setPreview()
            getResult()
        })
    }

    fun getResult() {
        (fragmentManager?.fragments?.get(0) as LottoFragment?)
            ?.crawl
            ?.getResult(
                gamelist, { setResult(it) }, { setCount(it) }
            )
    }

    fun setResult(params: List<Any>) {

        var nums: List<String>

        (fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment).apply {
            nums = this.nums
            balance.isVisible = false
            result.isVisible = true
            result.text = params[0] as String
        }

        (recycler.adapter as GamelistAdapter).apply {
            resultList.clear()
            resultList.addAll(params[1] as List<String>)

            lottoNum.clear()
            lottoNum.addAll(nums)
            notifyDataSetChanged()
        }
    }

    fun setCount(len: Int) {

        (fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment).apply {
            balance.isVisible = false
            result.isVisible = true
            result.text = getString(R.string.money_how_much).replace("$", len.toString())
        }

        (recycler.adapter as GamelistAdapter).apply {
            resultList.clear()
            lottoNum.clear()
            notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        setPreview()
        getResult()
        setFab()
    }

    private fun setPreview() {
        (activity as MainActivity).apply {
            if (gamelist.isNotEmpty()) {
                view?.preview?.text = ""
                return
            }
            view?.preview?.text = PREVIEWTEXT
        }
    }

    private fun setFab() {
        (activity as MainActivity).fab.also {
            val icon = when (preferences.getLatestIndex()) {
                0 -> R.drawable.ic_input_method_auto
                1 -> R.drawable.ic_input_method_manual
                2 -> R.drawable.ic_input_method_qr
                else -> throw error("no such index")
            }
            it.setImageResource(icon)
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
            .setOnClickListener {
                makeBottomSheetFragment(null)
            }
    }

    private fun makeBottomSheetFragment(game: Game?) {
        GamelistAddFragment(
            game,
            activity as MainActivity,
            recycler,
            gamelistViewModel
        )
            .also {
                it.show(fragmentManager!!, it.tag)
            }
    }
}