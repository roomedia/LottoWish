package com.roomedia.lottowish.ui.wish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.roomedia.lottowish.databinding.ActivityAddBinding
import com.roomedia.lottowish.room.wishlist.Wish
import com.roomedia.lottowish.utils.CurrencyTextNotEmptyWatcher
import com.roomedia.lottowish.utils.CurrencyTextWatcher
import com.roomedia.lottowish.utils.longToString
import com.roomedia.lottowish.utils.makeBriefCost
import com.roomedia.lottowish.utils.setUpAds
import com.roomedia.lottowish.utils.stringToLong

class WishActivity : AppCompatActivity() {

    var id = 0
    private var priority = MutableLiveData<Int>()
    private val binding by lazy { ActivityAddBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.briefCostText.hint = ""

        getWish()?.also { wish ->
            id = wish.id
            priority.value = wish.priority

            binding.name.setText(wish.title)
            binding.cost.setText(longToString(wish.cost))
            binding.briefCostText.hint = makeBriefCost(
                this,
                wish.cost
            )

            binding.deleteButton.visibility = View.VISIBLE
            binding.deleteButton.setOnClickListener { _ ->
                handleClick(Activity.RESULT_CANCELED)
            }
            binding.ok.isEnabled = true
            // pic
        } ?: run {
            priority.value = (getWishlist()?.size ?: 0) + 1
            binding.deleteButton.visibility = View.GONE
        }

        binding.name.addTextChangedListener(
            CurrencyTextNotEmptyWatcher(
                binding.ok,
                mutableListOf(binding.name, binding.cost),
                1
            )
        )
        binding.name.requestFocus()
        binding.name.selectAll()

        binding.cost.addTextChangedListener(
            CurrencyTextWatcher(
                binding.ok,
                mutableListOf(binding.name, binding.cost),
                1,
                this,
                binding.briefCostText,
                binding.cost
            )
        )

        binding.cost.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.cost.selectAll()
            }
        }

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.ok.setOnClickListener {
            handleClick(Activity.RESULT_OK)
        }
        setUpAds(binding.adView)
    }

    private fun handleClick(resultCode: Int) {
        Intent().putExtra(
            "wish",
            Wish(
                id,
                binding.name.text.toString(),
                stringToLong(binding.cost.text),
                priority.value!!
            )
        ).let {
            setResult(resultCode, it)
            finish()
        }
    }

    private fun getWishlist(): ArrayList<Wish>? =
        intent.getParcelableArrayListExtra("wishlist")

    private fun getWish(): Wish? =
        intent.getParcelableExtra("wish")


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
