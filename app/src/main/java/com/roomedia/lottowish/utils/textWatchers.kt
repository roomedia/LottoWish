package com.roomedia.lottowish.utils

import android.content.Context
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

open class TextNotEmptyWatcher(
    val target: View,
    val editTexts: MutableList<EditText>,
) : TextWatcher {
    val texts = mutableListOf<String>()

    override fun beforeTextChanged(
        seq: CharSequence?,
        start: Int,
        count: Int,
        after: Int,
    ) = Unit

    override fun onTextChanged(
        seq: CharSequence?,
        start: Int,
        before: Int,
        count: Int,
    ) {
        enableOKButton()
    }

    override fun afterTextChanged(seq: Editable) = Unit

    open fun enableOKButton(): Boolean {
        texts.clear()
        texts.addAll(
            editTexts.map { it.text.toString() }.toMutableList(),
        )
        target.isEnabled =
            texts.map { it.isNotEmpty() }.reduce { acc, bool ->
                acc.and(bool)
            }
        return target.isEnabled
    }
}

open class CurrencyTextNotEmptyWatcher(
    target: Button,
    editTexts: MutableList<EditText>,
    val currencyIndex: Int,
) : TextNotEmptyWatcher(target, editTexts) {
    override fun enableOKButton(): Boolean {
        target.isEnabled =
            super
                .enableOKButton()
                .and(texts[currencyIndex] != "₩0")
        return target.isEnabled
    }
}

class CurrencyTextWatcher(
    target: Button,
    editTexts: MutableList<EditText>,
    currencyIndex: Int,
    val context: Context,
    private val briefCostText: TextView,
    private val cost: EditText,
) : CurrencyTextNotEmptyWatcher(target, editTexts, currencyIndex) {
    private var isSetText = false

    override fun onTextChanged(
        seq: CharSequence?,
        start: Int,
        before: Int,
        count: Int,
    ) {
        if (isSetText) {
            isSetText = false

            if (texts.isNotEmpty()) {
                texts.removeAt(currencyIndex)
            }
            texts.add(seq.toString())
            enableOKButton()
            return
        }

        val seqLoad =
            CoroutineScope(Dispatchers.IO).async {
                when (before) {
                    0 -> Regex("[^0-9₩,]").replace(seq!!, "")
                    else -> {
                        Regex("[0-9]{4,6}").find(seq!!)?.let {
                            seq.replaceRange(it.range.last - 3, it.range.last - 2, "")
                        } ?: seq
                    }
                }.let {
                    stringToLong(it)
                }.let {
                    isSetText = true
                    val text = longToString(it)
                    val diff = text.length - seq.toString().length
                    val selection =
                        when (text.length) {
                            2 -> 2
                            else -> 1
                        }.run {
                            max(this, start + count + diff)
                        }

                    Triple(
                        makeBriefCost(context, it),
                        text,
                        selection,
                    )
                }
            }

        CoroutineScope(Dispatchers.Main).launch {
            val (hint, text, selection) = seqLoad.await()
            briefCostText.hint = hint
            cost.setText(text)
            runCatching {
                cost.setSelection(selection)
            }
        }
    }

    override fun afterTextChanged(seq: Editable) = Unit
}

class NumberTextWatcher(
    target: View,
    editTexts: MutableList<EditText>,
    private val position: Int,
) : TextNotEmptyWatcher(target, editTexts) {
    init {
        editTexts[position].setOnKeyListener { v, keyCode, event ->
            if ((v as EditText).text.isNotEmpty()) {
                return@setOnKeyListener false
            }
            if (keyCode != KeyEvent.KEYCODE_DEL) {
                return@setOnKeyListener false
            }
            if (event.action == KeyEvent.ACTION_UP) {
                return@setOnKeyListener false
            }

            editTexts[max(position - 1, 0)].apply {
                requestFocus()
                setSelection(this.length())
            }
            true
        }
    }

    override fun onTextChanged(
        seq: CharSequence?,
        start: Int,
        before: Int,
        count: Int,
    ) {
        editTexts[position].setSelection(seq!!.length)
        nextFocus(seq.length)

        val wasEnabled = target.isEnabled
        val isEnabled = enableOKButton()
        if (wasEnabled == isEnabled) {
            return
        }

        (target as FloatingActionButton).drawable?.run {
            when (this) {
                is AnimatedStateListDrawable -> {
                    (this.current as AnimatedVectorDrawable).start()
                }

                is AnimatedStateListDrawableCompat -> {
                    (this.getCurrent() as AnimatedVectorDrawableCompat).start()
                }

                else -> {
                    error("Not this type")
                }
            }
        }
    }

    private fun nextFocus(len: Int) {
        if (len < 2) {
            return
        }
        editTexts[min(position + 1, editTexts.lastIndex)].apply {
            requestFocus()
            selectAll()
        }
    }

    override fun enableOKButton(): Boolean {
        texts.clear()
        texts.addAll(
            editTexts
                .map {
                    it.text.toString().run {
                        if (isNotEmpty()) {
                            this.toInt().toString()
                        } else {
                            this
                        }
                    }
                }.toMutableList(),
        )

        target.isEnabled =
            texts
                .map {
                    if (it == "") {
                        false
                    } else {
                        (it.toInt() <= 45)
                            .and(it.toInt() > 0)
                    }
                }.reduce { acc, bool ->
                    acc.and(bool)
                }

        return target.isEnabled
    }
}
