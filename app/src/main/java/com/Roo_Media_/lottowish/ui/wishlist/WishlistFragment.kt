package com.Roo_Media_.lottowish.ui.wishlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.wishlist.Wish
import com.Roo_Media_.lottowish.ui.LottoFragment
import com.Roo_Media_.lottowish.ui.MainActivity
import com.Roo_Media_.lottowish.ui.wish.WishActivity
import com.Roo_Media_.lottowish.utils.SmoothLayout
import com.Roo_Media_.lottowish.utils.makeBriefBalance
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lotto.*
import kotlinx.android.synthetic.main.fragment_recycler.*

class WishlistFragment : Fragment() {

    private val wishlist: ArrayList<Wish> by lazy { arrayListOf<Wish>() }
    private val wishlistViewModel: WishlistViewModel by lazy {
        ViewModelProviders.of(this)
            .get(WishlistViewModel::class.java)
    }

    private val REQUEST_INSERT_WISH = 1000
    private val REQUEST_UPDATE_WISH = 1001
    private val PREVIEWTEXT: String by lazy {
        getString(R.string.preview)
            .replace("$", getString(R.string.preview_wish))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_recycler, container, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        WishlistAdapter(context!!, wishlist).also {
            recycler.adapter = it
            recycler.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
            recycler.layoutManager =
                SmoothLayout(context!!)
            recycler.setHasFixedSize(true)
        }
            .also { adapter ->
                wishlistViewModel.getAll().observe(
                    this,
                    Observer {
                        wishlist.clear()
                        wishlist.addAll(it!!)
                        setPreview()
                        setBalance()
                        adapter.notifyDataSetChanged()
                    }
                )
            }
            .also {
                it.updateWish.observe(
                    this,
                    Observer {
                        makeActivity(it)
                    }
                )
            }
            .also {
                it.deleteWish.observe(
                    this,
                    Observer {
                        wishlistViewModel.delete(it)
                    }
                )
            }
            .also {
                it.deleteAll.observe(
                    this,
                    Observer {
                        wishlistViewModel.deleteAll()
                    }
                )
            }

        val touchHelperCallback = WishlistTouchHelperCallback(
            wishlistViewModel,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            -1
        )
        val touchHelper = ItemTouchHelper(touchHelperCallback)
        touchHelper.attachToRecyclerView(recycler)
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
                preview?.text = ""
                return
            }
            preview?.text = PREVIEWTEXT
        }
    }

    private fun setFab() {
        (activity as MainActivity).fab.also {
            it.setImageResource(R.drawable.ic_input_method_add)
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
            .setOnClickListener {
                makeActivity(null)
            }
    }

    fun setBalance() {
        val adapter = recycler?.adapter as WishlistAdapter? ?: return
        val fragment = fragmentManager?.findFragmentById(R.id.lotto_fragment) as LottoFragment

        fragment.currentPrize?.also {
            val balance = adapter.setBalance(it)
            fragment.result.isVisible = false
            fragment.balance.isVisible = true
            fragment.balance.text = makeBriefBalance(
                context!!,
                balance
            )

            adapter.notifyDataSetChanged()
        }
    }

    private fun makeActivity(wish: Wish?) {
        Intent(context!!, WishActivity::class.java)
            .putExtra(
                "wishlist", when (wishlist.size) {
                    0 -> null
                    else -> wishlist
                }
            )
            .putExtra("wish", wish)
            .also {
                when (wish == null) {
                    true -> startActivityForResult(it, REQUEST_INSERT_WISH)
                    false -> startActivityForResult(it, REQUEST_UPDATE_WISH)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.getParcelableExtra<Wish>("wish")?.also { wish ->
            if (resultCode != Activity.RESULT_OK) {
                wishlistViewModel.delete(wish)
                recycler.adapter?.notifyItemRemoved(wish.priority - 1)
                return
            }

            when (requestCode) {
                REQUEST_INSERT_WISH -> {
                    wishlistViewModel.insert(wish)
                    recycler.smoothScrollToPosition(wishlist.size)
                }

                REQUEST_UPDATE_WISH -> {
                    wishlistViewModel.update(wish)
                }
            }
        }
    }
}