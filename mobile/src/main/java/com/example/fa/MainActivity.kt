package com.example.fa

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        button.setOnClickListener { sendMessageToClock() }

        startWearApp()

    }

    private fun startWearApp() {
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
    }

    private fun sendMessageToClock() {


        val putDataReq: PutDataRequest = PutDataMapRequest.create("/count")
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
        }

    }

    companion object {
        private const val COUNT_KEY = "com.example.key.count"
    }
}
