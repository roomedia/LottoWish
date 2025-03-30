package com.roomedia.lottowish.utils

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.roomedia.lottowish.R
import com.roomedia.lottowish.ui.MainActivity
import java.util.Calendar

class NotifyReceiver : BroadcastReceiver() {

    private val NOTIFICATION_ID = 1000
    private lateinit var context: Context
    private val channelID: String by lazy { context.getString(R.string.push_channel_id) }
    private val title: String by lazy { context.getString(R.string.push_title) }
    private val text: String by lazy { context.getString(R.string.push_text) }

    override fun onReceive(context: Context, intent: Intent) {

        val builder = init(context)
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                channelID,
                title,
                importance
            ).apply {
                description = text
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, builder)
    }

    private fun init(context: Context): Notification {
        this.context = context
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_input_method_auto)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
    }
}

fun makePushAlarm(context: Context) {

    val REQUEST_CODE = 1000

    val intent = Intent(context, NotifyReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 50)
    }

    (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).apply {
        this.cancel(pending)
        this.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pending
        )
    }
}
