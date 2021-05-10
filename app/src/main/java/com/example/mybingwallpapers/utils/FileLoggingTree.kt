package com.example.mybingwallpapers.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// rather do asynchronously: send a message to a buffer and flush there -
// or use a lib
class FileLoggingTree(private val applicationContext: Context) : DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        try {
            val fileNameTimeStamp: String = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())
            val fileName = "$fileNameTimeStamp.html"

            // Create file
            val file: File? = generateLogFile(fileName)

            // If file created or exists save logs
            if (file != null) {
                val writer = FileWriter(file, true)
                val logTimeStamp: String = SimpleDateFormat(
                        "E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                        Locale.getDefault()
                ).format(Date())
                writer.append(
                    "<p style=\"background:lightgray;\"><strong "
                            + "style=\"background:lightblue;\">&nbsp&nbsp"
                )
                    .append(logTimeStamp)
                    .append(" :&nbsp&nbsp</strong><strong>&nbsp&nbsp")
                    .append(tag)
                    .append("</strong> - ")
                    .append(message)
                    .append("</p>")
                writer.flush()
                writer.close()
            }
        } catch (e: Exception) {
            Timber.e(FileLoggingTree::class.java.simpleName, "Error while logging into file : $e")
        }
    }

    /*  Helper method to create file*/
    private fun generateLogFile(fileName: String): File? {
        val root = File(
                applicationContext.getExternalFilesDir(null)?.absolutePath,
            "Log"
        )
        var dirExists = true
        if (!root.exists()) {
            dirExists = root.mkdirs()
        }
        return if (dirExists) {
            val file = File(root, fileName)
            if (!file.exists()) {
                // when creating, check for old as well
                Handler(Looper.getMainLooper()).postDelayed( {
                    root.walk().forEach {
                        if (it.isFile && it.extension == "html" &&
                                System.currentTimeMillis() - it.lastModified() > TimeUnit.DAYS.toMillis(14)) {
                            it.delete()
                        }
                    }
                }, TimeUnit.MINUTES.toMillis(1))
            }
            file
        } else null
    }
}