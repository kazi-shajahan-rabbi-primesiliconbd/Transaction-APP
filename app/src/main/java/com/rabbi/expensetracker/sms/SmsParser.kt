package com.rabbi.expensetracker.sms

import com.rabbi.expensetracker.data.Transaction
import com.rabbi.expensetracker.data.TxSource
import com.rabbi.expensetracker.data.TxType

/**
 * Best-effort parser for transaction SMS from mobile banking (bKash, Nagad,
 * Rocket) and bank debit/credit alerts. It does not rely on one rigid
 * template per provider - instead it looks for a currency amount plus
 * keywords that indicate money moving in or out, which keeps it working
 * across the many slightly-different wordings banks use.
 */
object SmsParser {

    private val amountRegex = Regex(
        "(?:tk|taka|bdt|rs)\\.?\\s?([0-9][0-9,]*\\.?[0-9]{0,2})|([0-9][0-9,]*\\.?[0-9]{0,2})\\s?(?:tk|taka|bdt|rs)\\b",
        RegexOption.IGNORE_CASE
    )

    private val expenseKeywords = listOf(
        "debited", "cash out", "payment", "sent tk", "you sent", "paid",
        "purchase", "withdrawn", "deducted", "charged", "bill pay", "send money"
    )

    private val incomeKeywords = listOf(
        "credited", "received", "you have received", "deposit", "refund",
        "cashback", "cash in", "salary", "money received"
    )

    // Keyword -> category guess, checked in order.
    private val categoryHints = linkedMapOf(
        "cash out" to "Cash Out",
        "send money" to "Send Money",
        "recharge" to "Mobile Recharge",
        "bill" to "Bill Payment",
        "salary" to "Salary",
        "refund" to "Refund",
        "cashback" to "Refund",
        "grocery" to "Groceries",
        "restaurant" to "Food & Dining",
        "merchant" to "Shopping"
    )

    // Only treat a message as a transaction if it's from a recognizable
    // financial sender or clearly contains an amount + a strong keyword.
    private val trustedSenderHints = listOf(
        "bkash", "nagad", "rocket", "bank", "dbbl", "brac", "citybank",
        "ebl", "islami", "prime bank", "mtb", "ucb", "trust"
    )

    fun parse(sender: String, body: String, timestampMillis: Long): Transaction? {
        val lower = body.lowercase()
        val senderLower = sender.lowercase()

        val hasExpenseWord = expenseKeywords.any { lower.contains(it) }
        val hasIncomeWord = incomeKeywords.any { lower.contains(it) }
        if (!hasExpenseWord && !hasIncomeWord) return null

        val looksFinancial = trustedSenderHints.any { senderLower.contains(it) } ||
            lower.contains("tk") || lower.contains("bdt") || lower.contains("balance")
        if (!looksFinancial) return null

        val match = amountRegex.find(lower) ?: return null
        val rawAmount = (match.groupValues[1].ifBlank { match.groupValues[2] })
            .replace(",", "")
        val amount = rawAmount.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        val type = if (hasExpenseWord && !hasIncomeWord) TxType.EXPENSE
        else if (hasIncomeWord && !hasExpenseWord) TxType.INCOME
        // If both keyword sets somehow match, trust whichever keyword
        // appears first in the message.
        else {
            val expenseIdx = expenseKeywords.minOf { kw -> lower.indexOf(kw).let { if (it < 0) Int.MAX_VALUE else it } }
            val incomeIdx = incomeKeywords.minOf { kw -> lower.indexOf(kw).let { if (it < 0) Int.MAX_VALUE else it } }
            if (expenseIdx <= incomeIdx) TxType.EXPENSE else TxType.INCOME
        }

        val category = categoryHints.entries.firstOrNull { lower.contains(it.key) }?.value
            ?: if (type == TxType.INCOME) "Transfer" else "Other"

        return Transaction(
            amount = amount,
            type = type,
            category = category,
            note = body.trim(),
            sender = sender,
            timestamp = timestampMillis,
            source = TxSource.SMS
        )
    }
}
