package com.vsb.tamz.osmz_http_server

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData

class StatsLiveData: MutableLiveData<RequestMetric>() {
    companion object {
        private lateinit var sInstance: StatsLiveData

        @MainThread
        fun get(): StatsLiveData {
            sInstance = if (::sInstance.isInitialized) sInstance else StatsLiveData()
            return sInstance
        }
    }
}