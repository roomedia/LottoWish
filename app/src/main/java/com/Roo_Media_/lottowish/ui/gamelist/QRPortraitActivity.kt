package com.Roo_Media_.lottowish.ui.gamelist

import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity

class QRPortraitActivity : CaptureActivity()

class FragmentIntentIntegrator(
    private val fragment: GamelistAddFragment
) : IntentIntegrator(fragment.activity) {

    override fun startActivityForResult(intent: Intent, code: Int) {
        fragment.startActivityForResult(intent, code)
    }
}