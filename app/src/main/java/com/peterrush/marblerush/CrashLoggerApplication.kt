package com.peterrush.marblerush

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashLoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val writer = StringWriter()
                error.printStackTrace(PrintWriter(writer))
                File(filesDir, "last_crash.txt").writeText(
                    "Thread: ${thread.name}\n${writer}"
                )
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
