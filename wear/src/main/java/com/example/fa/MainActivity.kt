package com.example.fa

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {

    private val wearClient: DataClient by lazy { Wearable.getDataClient(this)}
    private val capabiltyClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        wearClient.addListener { dataEventBuffer -> handleData(dataEventBuffer) }
    }

    override fun onStart() {
        super.onStart()
        capabiltyClient.addLocalCapability("TestCapability1")
        capabiltyClient.addLocalCapability("TestCapability2")
    }

    override fun onStop() {
        capabiltyClient.removeLocalCapability("TestCapability1")
        capabiltyClient.removeLocalCapability("TestCapability2")
        super.onStop()
    }

    private fun handleData(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // DataItem changed
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    event.dataItem.also { item ->
                        if (item.uri.path?.compareTo("/count") == 0) {
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

    private fun updateCount(counter: Int) {
        text.text = counter.toString(16)
    }


    companion object {
        private const val COUNT_KEY = "com.example.key.count"
    }
}
