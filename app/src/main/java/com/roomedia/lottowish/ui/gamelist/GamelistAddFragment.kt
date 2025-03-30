package com.roomedia.lottowish.ui.gamelist

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.integration.android.IntentIntegrator
import com.roomedia.lottowish.R
import com.roomedia.lottowish.databinding.FragmentAddGameBinding
import com.roomedia.lottowish.room.gamelist.Game
import com.roomedia.lottowish.ui.MainActivity
import com.roomedia.lottowish.utils.NumberTextWatcher
import com.roomedia.lottowish.utils.Preferences
import com.roomedia.lottowish.utils.setUpAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GamelistAddFragment(
    private var game: Game?,
    val activity: MainActivity,
    private val recycler: RecyclerView,
    private val gamelistViewModel: GamelistViewModel
) : BottomSheetDialogFragment() {

    private var binding: FragmentAddGameBinding? = null
    private val inputs: List<EditText> by lazy {
        binding?.inputContainer?.children?.toList()?.filterIsInstance<EditText>()
            ?: emptyList()
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
        binding?.adView?.let { setUpAds(it) }
        setFab()
    }

    private fun setModeChangeListener() {
        binding?.mode?.onItemSelectedListener = ItemSelectedListener()
        game?.let {
            preferences.setLatestIndex(1)
            binding?.mode?.setSelection(1)
            for ((input, num) in inputs zip it.number.split(" ")) {
                input.setText(num)
            }
        } ?: binding?.mode?.setSelection(preferences.getLatestIndex())
    }

    inner class ItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            binding?.mode?.selectedItemPosition?.let { preferences.setLatestIndex(it) }
            setInputMethod()
        }
    }

    private fun setInputListener() {
        for ((idx, input) in inputs.withIndex()) {
            input.setSelectAllOnFocus(true)
            input.addTextChangedListener(
                binding?.fab2?.let {
                    NumberTextWatcher(
                        it,
                        inputs.toMutableList(),
                        idx
                    )
                }
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
        activity.binding.fab.setImageResource(R.drawable.ic_input_method_auto)
        binding?.fab2?.setImageResource(R.drawable.ic_input_method_auto_selector)
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
        activity.binding.fab.setImageResource(R.drawable.ic_input_method_manual)
        binding?.fab2?.setImageResource(R.drawable.ic_input_method_manual_selector)

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
                    requireView().windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
        }
    }

    private fun setQRInputMethod() {
        for (input in inputs) {
            input.text = null
            input.isEnabled = false
        }
        activity.binding.fab.setImageResource(R.drawable.ic_input_method_qr)
        binding?.fab2?.setImageResource(R.drawable.ic_input_method_qr_selector)
        binding?.fab2?.isEnabled = true
        closeKeyboard()
    }

    private fun setFab() {
        binding?.fab2?.setOnClickListener {
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
        binding = null
    }
}
