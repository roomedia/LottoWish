package com.Roo_Media_.lottowish.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.ui.gamelist.GamelistFragment
import com.Roo_Media_.lottowish.ui.wishlist.WishlistFragment
import com.Roo_Media_.lottowish.utils.makePushAlarm
import com.Roo_Media_.lottowish.utils.setUpAds
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private data class Data(val fragment: Fragment, val navId: Int)

    private val datas: ArrayList<Data> by lazy {
        arrayListOf(
            Data(
                WishlistFragment(),
                R.id.navigation_wishlist
            ),
            Data(
                GamelistFragment(),
                R.id.navigation_gamelist
            )
        )
    }
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view_pager.adapter = PagerAdapter(supportFragmentManager, lifecycle)
        view_pager.registerOnPageChangeCallback(PageChangeCallback())
        nav_view.setOnNavigationItemSelectedListener { navigationSelected(it) }
        makePushAlarm(this)
        setUpAds(adView)
    }

    private inner class PagerAdapter(fm: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fm, lc) {

        override fun getItemCount() = datas.size
        override fun createFragment(position: Int) = datas[position].fragment
    }

    private inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            nav_view.selectedItemId = datas[position].navId
            index = position
        }
    }

    private fun navigationSelected(item: MenuItem): Boolean {

        view_pager.currentItem =
            item.setChecked(true).itemId.let {
                index = it
                datas.map {
                    it.navId
                }.indexOf(it)
            }

        return true
    }

    fun getFragment(): Fragment {
        return datas[index].fragment
    }
}
