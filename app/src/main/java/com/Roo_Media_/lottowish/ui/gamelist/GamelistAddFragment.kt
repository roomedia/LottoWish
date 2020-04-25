package com.Roo_Media_.lottowish.ui.gamelist

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.Roo_Media_.lottowish.R
import com.Roo_Media_.lottowish.room.gamelist.Game
import com.Roo_Media_.lottowish.ui.MainActivity
import com.Roo_Media_.lottowish.utils.NumberTextWatcher
import com.Roo_Media_.lottowish.utils.Preferences
import com.Roo_Media_.lottowish.utils.setUpAds
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_game.*
import kotlinx.android.synthetic.main.fragment_add_game.adView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GamelistAddFragment(
    var game: Game?,
    val activity: MainActivity,
    val recycler: RecyclerView,
    val gamelistViewModel: GamelistViewModel
) : BottomSheetDialogFragment() {

    private val inputs: List<EditText> by lazy {
        inputContainer.children.toList().filterIsInstance<EditText>()
    }

    private val preferences: Preferences by lazy { Preferences(activity) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contextThemeWrapper =
            ContextThemeWrapper(activity, R.style.com_Roo_Media__lottowish_AppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.fragment_add_game, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setModeChangeListener()
        setInputListener()
        setUpAds(adView)
        setFab()
    }

    private fun setModeChangeListener() {
        mode.onItemSelectedListener = ItemSelectedListener()
        game?.let {
            preferences.setLatestIndex(1)
            mode.setSelection(1)
            for ((input, num) in inputs zip it.number.split(" ")) {
                input.setText(num)
            }
        } ?: mode.setSelection(preferences.getLatestIndex())
    }

    inner class ItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            preferences.setLatestIndex(mode.selectedItemPosition)
            setInputMethod()
        }
    }

    private fun setInputListener() {
        for ((idx, input) in inputs.withIndex()) {
            input.setSelectAllOnFocus(true)
            input.addTextChangedListener(
                NumberTextWatcher(
                    fab2,
                    inputs.toMutableList(),
                    idx
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setInputMethod()
    }

    private fun setInputMethod() {
        when (preferences.getLatestIndex()) {
            0 -> setAutoInputMethod()
            1 -> setManualInputMethod()
            2 -> setQRInputMethod()
            else -> return
        }
    }

    private fun setAutoInputMethod() {
        val nums = randomNum()
        for ((input, num) in (inputs zip nums)) {
            input.isEnabled = true
            input.setText(num.toString())
        }
        inputs[0].requestFocus()
        activity.fab.setImageResource(R.drawable.ic_input_method_auto)
        fab2.setImageResource(R.drawable.ic_input_method_auto_selector)
    }

    private fun randomNum(): List<Int> {
        val list: MutableList<Int> = mutableListOf()
        do {
            val random = (Math.random() * 100 % 45 + 1).toInt()
            if (list.indexOf(random) != -1) continue
            list.add(random)
        } while (list.size < 6)

        return list.sorted()
    }

    private fun setManualInputMethod() {
        openKeyboard()
        activity.fab.setImageResource(R.drawable.ic_input_method_manual)
        fab2.setImageResource(R.drawable.ic_input_method_manual_selector)

        for (input in inputs) {
            if (game == null) {
                input.setText("")
            }
            input.isEnabled = true
        }
    }

    private fun openKeyboard() {
        CoroutineScope(Dispatchers.Main).launch {
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            inputs[0].requestFocus()
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(
                    inputs[0],
                    InputMethodManager.SHOW_IMPLICIT
                )
        }
    }

    private fun closeKeyboard() {
        CoroutineScope(Dispatchers.Main).launch {
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(
                    view!!.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
        }
    }

    private fun setQRInputMethod() {
        for (input in inputs) {
            input.text = null
            input.isEnabled = false
        }
        activity.fab.setImageResource(R.drawable.ic_input_method_qr)
        fab2.setImageResource(R.drawable.ic_input_method_qr_selector)
        fab2.isEnabled = true
        closeKeyboard()
    }

    private fun setFab() {
        fab2.setOnClickListener {
            when (preferences.getLatestIndex()) {
                0, 1 -> inputNumbers()
                2 -> inputFromQR()
            }
        }
    }

    private fun inputNumbers() {
        val nums = inputs.map {
            it.text.toString().toInt()
        }.sorted().joinToString(" ")

        game?.let {
            gamelistViewModel.update(Game(it.id, nums))
            game = null
        } ?: let {
            gamelistViewModel.insert(Game(0, nums))
            recycler.layoutManager?.smoothScrollToPosition(
                recycler,
                RecyclerView.State(),
                (gamelistViewModel.getAll().value?.size ?: 1) - 1
            )
        }
        setInputMethod()
    }

    private fun inputFromQR() {
        FragmentIntentIntegrator(this).also {
            it.setRequestCode(IntentIntegrator.REQUEST_CODE)
            it.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            it.captureActivity = QRPortraitActivity::class.java
            it.setOrientationLocked(true)
            it.setBeepEnabled(true)
            it.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != IntentIntegrator.REQUEST_CODE) return
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data).also { result ->
            if (result.contents == null) return
            result.contents.split("q").let { list ->
                list.subList(1, list.lastIndex + 1).forEach { string ->
                    val nums = string.substring(0, 12).replace("[0-9]{2}".toRegex()) {
                        it.value.replace("0[0-9]".toRegex()) { it.value.substring(1) } + " "
                    }.trim()
                    gamelistViewModel.insert(Game(0, nums))
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        closeKeyboard()
        super.onDismiss(dialog)
    }
}