package com.Roo_Media_.lottowish

import android.app.Application
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}
    }
}