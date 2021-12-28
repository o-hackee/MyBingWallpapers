package com.example.mybingwallpapers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.mybingwallpapers.databinding.ActivityMainBinding
import com.example.mybingwallpapers.ui.MySettingsFragment
import com.example.mybingwallpapers.ui.WorkStatusViewModel
import com.example.mybingwallpapers.utils.FileLoggingTree

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()

        // Get the ViewModel
        val viewModel = ViewModelProvider(this).get(WorkStatusViewModel::class.java)
        binding.viewModel = viewModel

        createChannel(
            getString(R.string.mbw_notification_channel_id),
            getString(R.string.mbw_notification_channel_name)
        )

        binding.root.setOnClickListener(object : View.OnClickListener {
            private var lastClickTimestamp = 0L
            override fun onClick(v: View?) {
                val timestamp = System.currentTimeMillis()
                if (timestamp - lastClickTimestamp < 200L)
                    onDoubleClick()
                lastClickTimestamp = timestamp
            }

            private fun onDoubleClick() {
                binding.showLogButton.isVisible = true
                Handler(Looper.getMainLooper()).postDelayed( {
                    binding.showLogButton.isVisible = false
                }, 5000L)
            }
        })
        binding.showLogButton.setOnClickListener {
            viewLog()
            binding.showLogButton.isVisible = false
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = channelName

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun viewLog() {
        val rootDir = FileLoggingTree.getRootRir(this)
        if (!rootDir.exists())
            return
        val lastLog = rootDir.listFiles()?.maxByOrNull { it.lastModified() }
        if (lastLog == null) {
            Toast.makeText(this, "No log files found", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(FileProvider.getUriForFile(this, application.packageName + ".provider", lastLog), "text/html")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}