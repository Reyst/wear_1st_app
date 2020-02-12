package com.example.fa

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ben.shared.*
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this)}
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(this)}

    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            sendMessageToClock(MESSAGE_PATH_PHONE_COUNT, byteArrayOf(counter.toByte()))
            counter++
        }

        /*if (!Settings.System.canWrite(this)) {
            val goToSettings = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            goToSettings.data = Uri.parse("package:" + this.packageName)
            startActivity(goToSettings)
        }
*/
        sendMessageToClock(MESSAGE_PATH_PHONE_START)
    }

    private fun sendMessageToClock(msg: String, data: ByteArray? = null) {

        val capabilityInfoTask = capabilityClient.getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful()) {
                task.getResult()?.let { capabilityInfo ->

                    val nodeId = getNearbyNode(capabilityInfo)
                    sendMsg(nodeId, msg, data)
                    Log.d("INSPECT", ":::::: wearNodeId - $nodeId")
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

    override fun onStart() {
        super.onStart()
        messageClient.addListener { messageEvent ->
            if (messageEvent.path == MESSAGE_PATH_WEAR_CLEAR_COUNT) {
                counter = 0
                val msg = messageEvent.data.toString(Charset.defaultCharset())
                this.runOnUiThread{
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                sendMsg(messageEvent.sourceNodeId, MESSAGE_PATH_PHONE_COUNT, byteArrayOf(counter.toByte()))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        messageClient.removeListener {  }
    }
}
