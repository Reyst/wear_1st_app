package com.example.fa

import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.provider.Settings
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import com.ben.shared.CAPABILITY_PHONE_APP
import com.ben.shared.MESSAGE_PATH_WEAR_CLEAR_COUNT
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : WearableActivity() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this)}
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(this)}
    private val connectivityManager: ConnectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

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

        network()
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

    private fun network() {
        //val bandwidth: Int = connectivityManager.

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d("INSPECT", ":::::: Network onAvailable")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.d("INSPECT", ":::::: Network onUnavailable")
            }
        }

        val request: NetworkRequest = NetworkRequest.Builder().run {
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            build()
        }

        if (Settings.System.canWrite(this)) {
            connectivityManager.requestNetwork(request, networkCallback)
        } else {
            val goToSettings = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            goToSettings.data = Uri.parse("package:" + this.packageName)
            startActivity(goToSettings)
        }
    }

    private fun updateCount(counter: Int) {
        text.text = counter.toString(16)
    }
}
