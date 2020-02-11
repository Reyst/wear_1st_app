package com.example.fa

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import com.ben.shared.*
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this)}
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAmbientEnabled()

        StartAppService.getMsg = {
            this.runOnUiThread(Runnable {
                updateCount(it.data.get(0).toInt())
            })
        }

        clear_btn.setOnClickListener {
            sendMessageToPhone(MESSAGE_PATH_WEAR_CLEAR_COUNT, "message from wearable".toByteArray())
        }
    }

    private fun sendMessageToPhone(msg: String, data: ByteArray? = null) {

        val capabilityInfoTask = capabilityClient.getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful()) {
                task.getResult()?.let { capabilityInfo ->

                    val nodeId = getNearbyNode(capabilityInfo)
                    sendMsg(nodeId, msg, data)
                    Log.d("INSPECT", ":::::: phoneNodeId - $nodeId")
                }
            } else {
                Toast.makeText(this@MainActivity, "capabilityInfoTask ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getNearbyNode(info: CapabilityInfo): String {
        var id = ""
        info.nodes.iterator().forEach {
            if (it.isNearby)
                return it.id

            id = it.id
        }
        return id
    }

    private fun sendMsg(nodeId: String, msg: String, bytes: ByteArray? = null) {

        val sendMessageTask = messageClient.sendMessage(nodeId, msg, bytes)

        sendMessageTask.addOnCompleteListener {
            val text = when {
                it.isCanceled -> "Canceled"
                it.isSuccessful -> "Success"
                else -> "Fail: ${it.exception?.message}"
            }
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateCount(counter: Int) {
        text.text = counter.toString(16)
    }
}
