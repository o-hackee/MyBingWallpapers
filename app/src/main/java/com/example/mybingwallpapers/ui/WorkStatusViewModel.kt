package com.example.mybingwallpapers.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mybingwallpapers.work.GetImageWorker
import com.example.mybingwallpapers.work.PeriodicWorkStarter
import com.example.mybingwallpapers.work.PeriodicWorker

class WorkStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val periodicWorkStarterInfos: LiveData<List<WorkInfo>> = WorkManager.getInstance(application).getWorkInfosForUniqueWorkLiveData(PeriodicWorkStarter.workName)
    private val periodicWorkerInfos: LiveData<List<WorkInfo>> = WorkManager.getInstance(application).getWorkInfosForUniqueWorkLiveData(PeriodicWorker.workName)
    private val getImageWorkerInfos: LiveData<List<WorkInfo>> = WorkManager.getInstance(application).getWorkInfosForUniqueWorkLiveData(GetImageWorker.workName)

    val displayPeriodicWorkStarterInfos = Transformations.map(periodicWorkStarterInfos) { workStatus(it, PeriodicWorkStarter.workName) }
    val displayPeriodicWorkerInfos = Transformations.map(periodicWorkerInfos) { workStatus(it, PeriodicWorker.workName) }
    val displayGetImageWorkerInfos = Transformations.map(getImageWorkerInfos) { workStatus(it, GetImageWorker.workName) }

    private fun workStatus(it: List<WorkInfo>, name: String): String {
        // If there are no matching work info, do nothing
        if (it.isNullOrEmpty()) {
            return "no '$name' work found"
        }
        val workInfo = it[0]
        return "$name: ${workInfo.state}"
    }
}
