package com.Roo_Media_.lottowish.utils

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

fun setUpAds(adView: AdView) {
    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)
    adView.adListener = AdListener()
}