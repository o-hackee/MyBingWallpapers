package com.example.mybingwallpapers

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class MySettingsFragment : PreferenceFragmentCompat() {
    private val activeSettingKey = "active"
    private val workHour = 6
    private val workManager by lazy { WorkManager.getInstance(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val signaturePreference: SwitchPreferenceCompat? = findPreference(activeSettingKey)
        signaturePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener(::onPreferenceChange)

    }

    private fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key != activeSettingKey)
            return true
        val isCurrentlyActive = preference.sharedPreferences.getBoolean(activeSettingKey, false)
        if (isCurrentlyActive) {
            if (newValue == false) {
                cancelJob()
                return true
            }
        } else {
            if (newValue == true) {
                startJob()
                return true
            }
        }

        return true
    }

    private fun startJob() {
        // start one-time job for getting - now
        GetImageWorker.start(requireContext())

        // start one-time job for scheduling
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY).toLong()
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)
        val hours = if (hour < workHour)
            workHour.toLong()
        else
            workHour + 24L
        val delaySec = TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(TimeUnit.HOURS.toMinutes(hour) + minute) - second
        val startSchedulerWork = OneTimeWorkRequestBuilder<PeriodicWorkStarter>()
            .setInitialDelay(delaySec, TimeUnit.SECONDS)
            // leave backoff default
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(PeriodicWorkStarter.workName,
            ExistingWorkPolicy.REPLACE,
            startSchedulerWork)
        // from there - start periodic job
    }

    private fun cancelJob() {
        // val infos = workManager.getWorkInfosForUniqueWork(workName)
        // infos.get().firstOrNull()?.state
        workManager.cancelUniqueWork(PeriodicWorkStarter.workName)
        workManager.cancelUniqueWork(PeriodicWorker.workName)
        workManager.cancelUniqueWork(GetImageWorker.workName)
    }

}
