package com.example.fa

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ben.shared.*
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class StartAppService: WearableListenerService()  {

    override fun onMessageReceived(messageEvent: MessageEvent) {

        Log.wtf("INSPECT", messageEvent.toString())

        super.onMessageReceived(messageEvent)
        if (messageEvent.path == MESSAGE_PATH_PHONE_START) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            getMsg.invoke(messageEvent)
        }
    }

    companion object {
        const val START_ACTIVITY_PATH = "/start/app"
        var getMsg: (msg: MessageEvent) -> Unit = {}
    }
}