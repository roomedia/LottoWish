package com.roomedia.lottowish.ui.wishlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.FragmentRecyclerBinding
import com.roomedia.lottowish.room.wishlist.Wish
import com.roomedia.lottowish.ui.LottoFragment
import com.roomedia.lottowish.ui.MainActivity
import com.roomedia.lottowish.ui.wish.WishActivity
import com.roomedia.lottowish.utils.SmoothLayout
import com.roomedia.lottowish.utils.makeBriefBalance

class WishlistFragment : Fragment() {
    private val wishlist: ArrayList<Wish> by lazy { arrayListOf() }
    private val wishlistViewModel: WishlistViewModel by lazy {
        ViewModelProviders.of(this)[WishlistViewModel::class.java]
    }
    var binding: FragmentRecyclerBinding? = null

    private val REQUEST_INSERT_WISH = 1000
    private val REQUEST_UPDATE_WISH = 1001
    private val PREVIEWTEXT: String by lazy {
        getString(R.string.preview)
            .replace("$", getString(R.string.preview_wish))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        WishlistAdapter(requireContext(), wishlist)
            .also {
                binding!!.recycler.adapter = it
                binding!!.recycler.addItemDecoration(
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL),
                )
                binding!!.recycler.layoutManager =
                    SmoothLayout(requireContext())
                binding!!.recycler.setHasFixedSize(true)
            }.also { adapter ->
                wishlistViewModel.getAll().observe(viewLifecycleOwner) {
                    wishlist.clear()
                    wishlist.addAll(it!!)
                    setPreview()
                    setBalance()
                    adapter.notifyDataSetChanged()
                }
            }.also {
                it.updateWish.observe(viewLifecycleOwner) {
                    makeActivity(it)
                }
            }.also {
                it.deleteWish.observe(viewLifecycleOwner) {
                    wishlistViewModel.delete(it)
                }
            }.also {
                it.deleteAll.observe(viewLifecycleOwner) {
                    wishlistViewModel.deleteAll()
                }
            }

        val touchHelperCallback =
            WishlistTouchHelperCallback(
                wishlistViewModel,
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                -1,
            )
        val touchHelper = ItemTouchHelper(touchHelperCallback)
        touchHelper.attachToRecyclerView(binding!!.recycler)
    }

    override fun onResume() {
        super.onResume()
        setPreview()
        setBalance()
        setFab()
    }

    private fun setPreview() {
        (activity as MainActivity).apply {
            if (wishlist.isNotEmpty()) {
                this@WishlistFragment.binding?.preview?.text = ""
                return
            }
            this@WishlistFragment.binding?.preview?.text = PREVIEWTEXT
        }
    }

    private fun setFab() {
        (activity as MainActivity)
            .binding.fab
            .also {
                it.setImageResource(R.drawable.ic_input_method_add)
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }.setOnClickListener {
                makeActivity(null)
            }
    }

    fun setBalance() {
        val adapter = binding?.recycler?.adapter as WishlistAdapter? ?: return
        val fragment = fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment

        fragment.currentPrize?.also {
            val balance = adapter.setBalance(it)
            fragment.binding?.result?.isVisible = false
            fragment.binding?.balance?.isVisible = true
            fragment.binding?.balance?.text =
                makeBriefBalance(
                    requireContext(),
                    balance,
                )

            adapter.notifyDataSetChanged()
        }
    }

    private fun makeActivity(wish: Wish?) {
        Intent(requireContext(), WishActivity::class.java)
            .putExtra(
                "wishlist",
                when (wishlist.size) {
                    0 -> null
                    else -> wishlist
                },
            ).putExtra("wish", wish)
            .also {
                when (wish == null) {
                    true -> startActivityForResult(it, REQUEST_INSERT_WISH)
                    false -> startActivityForResult(it, REQUEST_UPDATE_WISH)
                }
            }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        data?.getParcelableExtra<Wish>("wish")?.also { wish ->
            if (resultCode != Activity.RESULT_OK) {
                wishlistViewModel.delete(wish)
                binding?.recycler?.adapter?.notifyItemRemoved(wish.priority - 1)
                return
            }

            when (requestCode) {
                REQUEST_INSERT_WISH -> {
                    wishlistViewModel.insert(wish)
                    binding?.recycler?.smoothScrollToPosition(wishlist.size)
                }

                REQUEST_UPDATE_WISH -> {
                    wishlistViewModel.update(wish)
                }
            }
        }
    }
}
