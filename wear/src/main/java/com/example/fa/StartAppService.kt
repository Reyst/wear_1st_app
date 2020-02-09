package com.example.fa

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class StartAppService: WearableListenerService()  {

    override fun onMessageReceived(messageEvent: MessageEvent) {

        Log.wtf("INSPECT", messageEvent.toString())

        super.onMessageReceived(messageEvent)
        if (messageEvent.path == START_ACTIVITY_PATH) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    companion object {
        const val START_ACTIVITY_PATH = "/start/app"
    }
}