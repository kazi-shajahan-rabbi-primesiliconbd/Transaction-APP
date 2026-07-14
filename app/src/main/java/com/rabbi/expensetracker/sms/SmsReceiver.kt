package com.rabbi.expensetracker.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rabbi.expensetracker.R
import com.rabbi.expensetracker.data.AppDatabase
import com.rabbi.expensetracker.data.TransactionRepository
import com.rabbi.expensetracker.data.TxType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val pendingResult = goAsync()
        val repo = TransactionRepository(AppDatabase.getInstance(context).transactionDao())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (msg in messages) {
                    val sender = msg.originatingAddress ?: continue
                    val body = msg.messageBody ?: continue
                    val parsed = SmsParser.parse(sender, body, System.currentTimeMillis())
                    if (parsed != null) {
                        val added = repo.addFromSms(parsed)
                        if (added) notifyUser(context, parsed.amount, parsed.type, parsed.category)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyUser(context: Context, amount: Double, type: TxType, category: String) {
        val channelId = "transactions"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Detected transactions", NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val sign = if (type == TxType.EXPENSE) "-" else "+"
        val title = if (type == TxType.EXPENSE) "Expense logged" else "Income logged"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("$sign৳${"%.2f".format(amount)} • $category")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted; transaction is still saved.
        }
    }
}
