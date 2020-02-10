package com.example.fa

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ben.shared.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this)}
    private val dataClient: DataClient by lazy { Wearable.getDataClient(this)}
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(this)}
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(this)}
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(this)}

    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { sendMessageToClock(MESSAGE_PATH_PHONE__COUNT) }

        //startWearApp()

        sendMessageToClock(MESSAGE_PATH_PHONE_START)
    }

    /*private fun startWearApp() {
        nodeClient.connectedNodes.addOnCompleteListener {
            if (it.isSuccessful) it.result?.forEach { node ->
                Log.wtf("INSPECT", "For node: $node")
                sendStartWearAppMessage(node)
            }
            else Log.e("INSPECT", it.exception?.message ?: "", it.exception)
        }

    }

    private fun sendStartWearAppMessage(node: Node) {

        val emptyBody = ByteArray(1).apply { set(0, 0) }

        node.takeIf { it.id != null }
            ?.id
            ?.let { nodeId ->
                messageClient.sendMessage(nodeId, "/start/app", emptyBody)
                    .addOnCompleteListener { msgTask ->
                        if (!msgTask.isSuccessful) {
                            Log.e("INSPECT", "Error: ${msgTask.exception?.message}", msgTask.exception)
                        }
                    }
            }
    }*/

    private fun sendMessageToClock(msg: String) {

        var phoneNodeId = ""

        val capabilityInfoTask = capabilityClient.getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener(object : OnCompleteListener<CapabilityInfo> {
            override fun onComplete(task: Task<CapabilityInfo>) {
                Log.d("INSPECT", ":::::: capabilityInfoTask - onComplete")
                if (task.isSuccessful()) {
                    Log.d("INSPECT", ":::::: capabilityInfoTask - isSuccessful")
                    task.getResult()?.let { capabilityInfo ->
                        capabilityInfo.nodes.iterator().forEach {
                            if (it.isNearby)
                                phoneNodeId = it.id
                        }

                        Log.d("INSPECT", ":::::: phoneNodeId - $phoneNodeId")
                        Log.d("INSPECT", ":::::: capabilityInfo.name - $capabilityInfo.name")

                        val sendMessageTask =
                            messageClient.sendMessage(phoneNodeId,
                                msg,
                                byteArrayOf(counter.toByte()))

                        counter++

                        sendMessageTask.addOnCompleteListener {
                            val text = when {
                                it.isCanceled -> "Canceled"
                                it.isSuccessful -> "Success"
                                else -> "Fail: ${it.exception?.message}"
                            }

                            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(this@MainActivity, "capabilityInfoTask ERROR", Toast.LENGTH_LONG).show()
                }
            }
        })

        /*val putDataReq: PutDataRequest = PutDataMapRequest.create("/count")
            .apply { dataMap.putInt(COUNT_KEY, counter++) }
            .asPutDataRequest()

        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)

        putDataTask.addOnCompleteListener {
            val text = when {
                it.isCanceled -> "Canceled"
                it.isSuccessful -> "Success"
                else -> "Fail: ${it.exception?.message}"
            }

            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }*/

    }

    companion object {
        private const val COUNT_KEY = "com.example.key.count"
    }
}
