package com.rabbi.expensetracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.rabbi.expensetracker.data.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun export(context: Context, transactions: List<Transaction>, label: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "taka_tracker_${label}.csv")
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        file.bufferedWriter().use { writer ->
            writer.write("Date,Type,Amount,Category,Source,Note\n")
            for (t in transactions) {
                val date = fmt.format(Date(t.timestamp))
                val note = t.note.replace("\"", "'").replace("\n", " ")
                writer.write("\"$date\",${t.type},${t.amount},${t.category},${t.source},\"$note\"\n")
            }
        }

        val uri = FileProvider.getUriForFile(context, "com.rabbi.expensetracker.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share transactions CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
