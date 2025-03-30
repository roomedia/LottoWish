package com.roomedia.lottowish.ui.gamelist

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.FragmentRecyclerBinding
import com.roomedia.lottowish.room.gamelist.Game
import com.roomedia.lottowish.ui.LottoFragment
import com.roomedia.lottowish.ui.MainActivity
import com.roomedia.lottowish.utils.Preferences
import com.roomedia.lottowish.utils.SmoothLayout

class GamelistFragment : Fragment() {
    private val gamelist = arrayListOf<Game>()
    private val gamelistViewModel: GamelistViewModel by lazy {
        ViewModelProviders
            .of(this)
            .get(GamelistViewModel::class.java)
    }

    private val preferences: Preferences by lazy { Preferences(requireActivity()) }
    private val previewText: String by lazy {
        getString(R.string.preview)
            .replace("$", getString(R.string.preview_game))
    }
    private var binding: FragmentRecyclerBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        return binding!!.recycler
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = GamelistAdapter(gamelist)
        binding?.recycler?.apply {
            this.adapter = adapter
            this.layoutManager =
                SmoothLayout(context)
            this.setHasFixedSize(true)
            this.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL),
            )
        }

        adapter.updateGame.observe(viewLifecycleOwner) {
            makeBottomSheetFragment(it)
        }

        adapter.deleteGame.observe(viewLifecycleOwner) {
            gamelistViewModel.delete(it)
        }

        adapter.deleteAll.observe(viewLifecycleOwner) {
            gamelistViewModel.deleteAll()
        }

        gamelistViewModel.getAll().observe(viewLifecycleOwner) {
            gamelist.clear()
            gamelist.addAll(it!!)

            setPreview()
            getResult()
        }
    }

    fun getResult() {
        (fragmentManager?.fragments?.get(0) as LottoFragment?)
            ?.crawl
            ?.getResult(
                gamelist,
                { setResult(it) },
                { setCount(it) },
            )
    }

    fun setResult(params: List<Any>) {
        var nums: List<String>

        (fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment).apply {
            nums = this.nums
            binding?.balance?.isVisible = false
            binding?.result?.isVisible = true
            binding?.result?.text = params[0] as String
        }

        (binding?.recycler?.adapter as? GamelistAdapter)?.apply {
            resultList.clear()
            resultList.addAll(params[1] as List<String>)

            lottoNum.clear()
            lottoNum.addAll(nums)
            notifyDataSetChanged()
        }
    }

    fun setCount(len: Int) {
        (fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment).apply {
            binding?.balance?.isVisible = false
            binding?.result?.isVisible = true
            binding?.result?.text = getString(R.string.money_how_much).replace("$", len.toString())
        }

        (binding?.recycler?.adapter as? GamelistAdapter)?.apply {
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
                this@GamelistFragment.binding?.preview?.text = ""
                return
            }
            this@GamelistFragment.binding?.preview?.text = previewText
        }
    }

    private fun setFab() {
        (activity as MainActivity)
            .binding.fab
            .also {
                val icon =
                    when (preferences.getLatestIndex()) {
                        0 -> R.drawable.ic_input_method_auto
                        1 -> R.drawable.ic_input_method_manual
                        2 -> R.drawable.ic_input_method_qr
                        else -> throw error("no such index")
                    }
                it.setImageResource(icon)
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }.setOnClickListener {
                makeBottomSheetFragment(null)
            }
    }

    private fun makeBottomSheetFragment(game: Game?) {
        GamelistAddFragment(
            game,
            activity as MainActivity,
            binding!!.recycler,
            gamelistViewModel,
        ).also {
            it.show(requireFragmentManager(), it.tag)
        }
    }
}
