package com.example.fa

import android.content.Context
import android.net.*
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import com.ben.shared.*
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
            this.runOnUiThread {
                updateCount(it.data.get(0).toInt())
            }
        }

        clear_btn.setOnClickListener {
            sendMessageToPhone(MESSAGE_PATH_WEAR_CLEAR_COUNT, "message from wearable".toByteArray())
        }

        networkIsConnected()
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

    private fun networkIsConnected() {

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("INSPECT", ":::::: Network onAvailable")
                super.onAvailable(network)
                /*Log.d("INSPECT", ":::::: Network onAvailable")
                this@MainActivity.runOnUiThread {
                    Glide.with(this@MainActivity)
                        .load("https://test-ipv6.com/images/hires_ok.png")
                        .into(imageView)
                }*/

                if (connectivityManager.bindProcessToNetwork(network)) {
                    Log.d("INSPECT", ":::::: Network isConnected")
                    Log.d("INSPECT", ":::::: Network ${
                        if (isNetworkHighBandwidth()) "FAST" else "SLOW"
                    }")

                } else
                    Log.d("INSPECT", ":::::: Network isDisconnected")
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

        connectivityManager.requestNetwork(request, networkCallback)
    }

    private fun isNetworkHighBandwidth() : Boolean {
        val network: Network? = connectivityManager.boundNetworkForProcess ?: connectivityManager.activeNetwork
        network?.let {
            val bandwidth = connectivityManager.getNetworkCapabilities(it)?.linkDownstreamBandwidthKbps
            bandwidth?.let { return it >= MIN_NETWORK_BANDWIDTH_KBPS }
        }
        return false
    }

    private fun updateCount(counter: Int) {
        text.text = counter.toString(16)
    }
}
