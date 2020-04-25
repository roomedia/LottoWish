package com.Roo_Media_.lottowish.ui.wish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.wishlist.Wish
import com.Roo_Media_.lottowish.utils.*
import kotlinx.android.synthetic.main.activity_add.*


class WishActivity : AppCompatActivity() {

    var id = 0
    var priority = MutableLiveData<Int>()

//    private val pictureViewModel: PictureViewModel by lazy {
//        ViewModelProviders.of(this)
//            .get(PictureViewModel::class.java)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        briefCostText.hint = ""

        getWish()?.also { wish ->
            id = wish.id
            priority.value = wish.priority

            name.setText(wish.title)
            cost.setText(longToString(wish.cost))
            briefCostText.hint = makeBriefCost(
                this,
                wish.cost
            )

            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener { _ ->
                handleClick(Activity.RESULT_CANCELED)
            }
            ok.isEnabled = true
            // pic
        } ?: run {
            priority.value = (getWishlist()?.size ?: 0) + 1
            deleteButton.visibility = View.GONE
        }

        name.addTextChangedListener(
            CurrencyTextNotEmptyWatcher(
                ok,
                mutableListOf(name, cost),
                1
            )
        )
        name.requestFocus()
        name.selectAll()

        cost.addTextChangedListener(
            CurrencyTextWatcher(
                ok,
                mutableListOf(name, cost),
                1,
                this,
                briefCostText,
                cost
            )
        )

        cost.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                cost.selectAll()
            }
        }

        cancel.setOnClickListener {
            finish()
        }

        ok.setOnClickListener {
            handleClick(Activity.RESULT_OK)
        }
        setUpAds(adView)
    }

    fun handleClick(resultCode: Int) {
        Intent().putExtra(
            "wish",
            Wish(
                id,
                name.text.toString(),
                stringToLong(cost.text),
                priority.value!!
            )
        ).let {
            setResult(resultCode, it)
            finish()
        }
    }

    fun getWishlist(): ArrayList<Wish>? =
        intent.getParcelableArrayListExtra("wishlist")

    fun getWish(): Wish? =
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