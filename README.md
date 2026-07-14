# Taka Tracker

A native Android app that tracks your income and spending automatically by reading
transaction SMS from bKash, Nagad, and bank alerts, plus manual entries for cash.

## Features
- Auto-detects income/expense from incoming SMS (bKash, Nagad, bank debit/credit alerts)
- One-tap import of your last 6 months of existing SMS history on first launch
- Manual add screen for cash transactions
- Dashboard: monthly balance, income, expenses, category pie chart
- Full transaction history with search, category filters, swipe-to-delete
- **Reset current month** button (Settings) — wipes that month's data with a confirmation dialog
- CSV export/share of any month's data
- Material You dynamic color + dark mode toggle
- Local-only storage (Room database) — nothing leaves your phone

## Easiest way: let GitHub build the APK for you (no software to install)

1. Go to https://github.com and make a free account if you don't have one
2. Click **+ → New repository**, name it anything (e.g. `taka-tracker`), keep it Public, click **Create repository**
3. On the new repo page, click **uploading an existing file**, then drag in **all the contents** of the unzipped `ExpenseTracker` folder (select everything inside it, not the folder itself) and click **Commit changes**
4. Click the **Actions** tab at the top of your repo — a build will start automatically (takes ~3–5 minutes; refresh to check progress)
5. Once it shows a green checkmark, click into that run, scroll to **Artifacts**, and download **TakaTracker-debug-apk** — it's a zip containing the `.apk`
6. Unzip that on your phone (or send the `.apk` straight to your phone via Google Drive/email/Telegram to yourself)
7. Tap the `.apk` file on your phone to install. Android will ask to allow installing from that app (Files/Chrome/etc.) — allow it once, then it installs like any app.

That's it — no Android Studio, no cables, no code.

## Alternative: build it yourself in Android Studio

1. Install **Android Studio** (free): https://developer.android.com/studio
2. Unzip this project, then in Android Studio: **File → Open** → select the `ExpenseTracker` folder
3. Let it sync (downloads Gradle + dependencies — needs internet, first sync takes a few minutes)
4. Plug your Android phone in via USB with **USB debugging** enabled (Settings → About phone → tap "Build number" 7 times → Developer options → USB debugging)
5. Select your device in the toolbar dropdown, click the green **Run ▶** button
6. The app installs and launches. Grant the SMS permission when asked — that's what lets it read transaction messages.

No USB cable? Build → Generate Signed Bundle/APK → APK, then copy the `.apk` file to your phone and tap it to install (you'll need to allow "install unknown apps" for your file manager once).

## Notes
- SMS detection is pattern-based (looks for amounts + words like "debited"/"received"/"cash out"). It covers common bKash/Nagad/bank wording but won't catch every possible phrasing — you can always add missed ones manually.
- iOS can't do this at all; Apple blocks any app from reading SMS.
- Everything is stored only in the app's local database — reinstalling or clearing app data erases it, so use CSV export if you want a backup.
