package com.example.fa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.wearable.activity.WearableActivity
import com.ben.shared.MESSAGE_PATH_PHONE_START
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {

    private val wearClient: DataClient by lazy { Wearable.getDataClient(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-onсдгг
        setAmbientEnabled()

        //wearClient.addListener { dataEventBuffer -> handleData(dataEventBuffer) }

        StartAppService.getMsg = {
            this.runOnUiThread(Runnable {
                updateCount(it.data.get(0).toInt())

            })
        }
    }

    private fun handleData(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // DataItem changed
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    event.dataItem.also { item ->
                        if (item.uri.path?.compareTo(MESSAGE_PATH_PHONE_START) == 0) {
                            DataMapItem.fromDataItem(item).dataMap.apply {
                                updateCount(getInt(COUNT_KEY))
                            }
                        }
                    }
                }
                DataEvent.TYPE_DELETED -> {
                    // DataItem deleted
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //wearClient.removeListener {}
    }

    private fun updateCount(counter: Int) {
        text.text = counter.toString(16)
    }


    companion object {
        private const val COUNT_KEY = "com.example.key.count"
    }
}
