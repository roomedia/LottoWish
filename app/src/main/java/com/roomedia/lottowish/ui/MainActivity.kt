package com.roomedia.lottowish.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.ActivityMainBinding
import com.roomedia.lottowish.ui.gamelist.GamelistFragment
import com.roomedia.lottowish.ui.wishlist.WishlistFragment
import com.roomedia.lottowish.utils.makePushAlarm
import com.roomedia.lottowish.utils.setUpAds

class MainActivity : AppCompatActivity() {

    private data class Data(val fragment: Fragment, val navId: Int)

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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
        setContentView(binding.root)

        binding.viewPager.adapter = PagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.registerOnPageChangeCallback(PageChangeCallback())
        binding.navView.setOnNavigationItemSelectedListener { navigationSelected(it) }
        makePushAlarm(this)
        setUpAds(binding.adView)
    }

    private inner class PagerAdapter(fm: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fm, lc) {

        override fun getItemCount() = datas.size
        override fun createFragment(position: Int) = datas[position].fragment
    }

    private inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.navView.selectedItemId = datas[position].navId
            index = position
        }
    }

    private fun navigationSelected(item: MenuItem): Boolean {
        binding.viewPager.currentItem =
            item.setChecked(true).itemId.let {
                index = it
                datas.map { data ->
                    data.navId
                }.indexOf(it)
            }

        return true
    }

    fun getFragment(): Fragment {
        return datas[index].fragment
    }
}
