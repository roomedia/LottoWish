package com.roomedia.lottowish.utils

import android.content.Context
import com.roomedia.lottowish.R
import java.text.NumberFormat
import kotlin.math.min

fun stringToLong(s: CharSequence?): Long =
    (s?.toString() ?: "₩0").let {
        NumberFormat.getNumberInstance().parse(
            when (it) {
                "", "₩" -> "0"
                else -> s!!.replace(Regex("[₩,]"), "")
            }
        ) as Long
    }.let {
        min(it, (1e13 - 1).toLong())
    }

fun longToString(n: Long?): String =
    NumberFormat.getCurrencyInstance().format(n)

fun makeRound(context: Context, molocule: Long, denominator: Double): String {
    return when (denominator) {
        1e0 -> R.string.unit1
        1e4 -> R.string.unit2
        1e8 -> R.string.unit3
        1e12 -> R.string.unit4
        else -> error("no such unit")
    }.run {
        context.getString(this)
    }.run {
        (molocule / denominator).toLong().let {
            longToString(it).trimStart('₩') + this
        }
    }
}

fun makeBriefCost(context: Context, cost: Long): String {
    return when (cost) {
        in 0..-1 + 1e4.toInt() -> 1e0
        in 1e4.toInt()..-1 + 1e8.toInt() -> 1e4
        in 1e8.toInt()..-1 + 1e12.toLong() -> 1e8
        else -> 1e12
    }.let {
        when (it) {
            1e0 -> ""
            else -> context.getString(R.string.assume) + " "
        } + makeRound(context, cost, it)
    }
}

fun makeBriefBalance(context: Context, balance: Long): String {
    val id: Int
    if (balance >= 0) {
        id = R.string.money_left
        balance
    } else {
        id = R.string.money_lack
        -balance
    }.let {
        return makeBriefCost(context, it) + " " + context.getString(id)
    }
}
