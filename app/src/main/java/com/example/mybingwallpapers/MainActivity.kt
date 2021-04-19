package com.example.mybingwallpapers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.mybingwallpapers.databinding.ActivityMainBinding
import com.example.mybingwallpapers.ui.MySettingsFragment
import com.example.mybingwallpapers.ui.WorkStatusViewModel

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
}