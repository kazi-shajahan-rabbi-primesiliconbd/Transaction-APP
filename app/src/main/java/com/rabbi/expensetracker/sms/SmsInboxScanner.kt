package com.rabbi.expensetracker.sms

import android.content.Context
import android.provider.Telephony
import com.rabbi.expensetracker.data.TransactionRepository

object SmsInboxScanner {

    /** Scans the device SMS inbox and imports any parseable transactions. Returns count added. */
    suspend fun scanInbox(context: Context, repository: TransactionRepository, sinceMillis: Long): Int {
        var added = 0
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        val selection = "${Telephony.Sms.DATE} >= ?"
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            arrayOf(sinceMillis.toString()),
            "${Telephony.Sms.DATE} ASC"
        )

        cursor?.use {
            val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            while (it.moveToNext()) {
                val sender = it.getString(addressIdx) ?: continue
                val body = it.getString(bodyIdx) ?: continue
                val date = it.getLong(dateIdx)
                val parsed = SmsParser.parse(sender, body, date)
                if (parsed != null && repository.addFromSms(parsed)) {
                    added++
                }
            }
        }
        return added
    }
}
