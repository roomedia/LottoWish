package com.roomedia.lottowish.utils

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.roomedia.lottowish.R
import com.roomedia.lottowish.room.gamelist.Game

class Crawl(
    val context: Context,
) {
    private val queue: RequestQueue by lazy { Volley.newRequestQueue(context) }
    private var count = 0
    private var position = 0

    fun getRound(
        count: Int,
        position: Int,
        callback: (params: List<Any>) -> Unit,
    ) {
        this.count = count
        this.position = position

        val (url, regex) =
            when (position == 0) {
                true ->
                    Pair(
                        context.getString(R.string.url_next),
                        context.getString(R.string.regex_next).toRegex(),
                    )
                false ->
                    Pair(
                        context.getString(R.string.url_result) + (count - position),
                        context.getString(R.string.regex_result).toRegex(),
                    )
            }

        StringRequest(
            Request.Method.GET,
            url,
            {
                getRoundListener(regex, callback, it)
            },
            {
                Log.d("error:", "parsing error")
            },
        ).also {
            queue.add(it)
        }
    }

    private fun getRoundListener(
        regex: Regex,
        callback: (params: List<Any>) -> Unit,
        response: String,
    ) {
        val regexResult = regex.find(response)!!.groups

        if (position == 0) {
            val prizeText =
                "${context.getString(R.string.money_expected)} ${regexResult[0]!!.value}"
            val currentPrize = stringToLong(regexResult[1]!!.value)

            callback(
                listOf(prizeText, currentPrize),
            )
        } else {
            val prizeText = regexResult[5]!!.value
            val currentPrize = stringToLong(regexResult[6]!!.value)
            val nums = regexResult[2]!!.value.split(",") + regexResult[4]!!.value

            callback(
                listOf(prizeText, currentPrize, nums),
            )
        }
    }

    fun getResult(
        gamelist: List<Game>,
        setResultCallback: (params: List<Any>) -> Unit,
        setCountCallback: (len: Int) -> Unit,
    ) {
        if (gamelist.isEmpty()) {
            setCountCallback(0)
            return
        }

        if (position == 0) {
            setCountCallback(gamelist.size)
            return
        }

        val url =
            context.getString(R.string.url_query) +
                makeQuery(count - position, gamelist)

        StringRequest(
            Request.Method.GET,
            url,
            {
                getResultListener(setResultCallback, it)
            },
            {
                Log.d("warning", "no parsed data")
            },
        ).also {
            queue.add(it)
        }
    }

    private fun makeQuery(
        round: Int,
        gamelist: List<Game>,
    ): String {
        val roundText = "%04d".format(round)

        return roundText + "q" +
            gamelist
                .map {
                    it.number.split(" ").joinToString("") {
                        it.padStart(2, '0')
                    }
                }.reduce { acc, s ->
                    "${acc}q$s"
                }
    }

    private fun getResultListener(
        setResultCallback: (params: List<Any>) -> Unit,
        response: String,
    ) {
        val resultText =
            (
                parseData(
                    R.string.regex_query,
                    response,
                ) {
                    "${it[1]!!.value} ${it[2]!!.value}".replace(
                        Regex("<(.+\"|/.+)>|\\."),
                        "",
                    )
                } as List<*>
            )[0] as String

        val eleResultTexts =
            parseData(R.string.regex_query_ele, response) {
                it[1]!!.value
            } as List<String>

        setResultCallback(listOf(resultText, eleResultTexts))
    }

    private fun parseData(
        resId: Int,
        response: String,
        lambda: (it: MatchGroupCollection) -> Any,
    ): Any =
        context.getString(resId).toRegex().findAll(response).toList().map {
            lambda(it.groups)
        }
}
