package com.h4pay.store

import com.h4pay.store.R
import android.R.id.icon_frame
import android.R.id.message
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class notiClass : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        FirebaseMessaging.getInstance().subscribeToTopic("orderNotify")
        if (p0 != null) {
            Log.d("FCM_TEST", p0)
        }
    }


    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        if (p0 != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("orderNotify")
            val title = p0.data.get("title")
            val message = p0.data.get("message")
            val category = p0.data.get("category")
            val orderID = p0.data.get("orderID")?.toDouble()
            var pendingIntent:PendingIntent? = null
            if (category =="orderNotify"){
                val intent = Intent(this, orderList::class.java)
                intent.putExtra("category", category)
                intent.putExtra("orderID", orderID)
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            var notificationBuilder: NotificationCompat.Builder? = null
            if (category != null) {
                Log.e("NotiTest", category)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (category == "orderNotify") {
                    val channel = "orderNotify"
                    val channel_nm = "주문 알림"

                    val notichannel =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelMessage = NotificationChannel(
                        channel, channel_nm,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    channelMessage.description = "주문 알림을 받습니다."
                    channelMessage.enableLights(true)
                    channelMessage.enableVibration(true)
                    channelMessage.setShowBadge(false)
                    channelMessage.vibrationPattern = longArrayOf(1000, 1000)
                    notichannel.createNotificationChannel(channelMessage)
                    //푸시알림을 Builder를 이용하여 만듭니다.

                    notificationBuilder = NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_shopping_bag)
                        .setContentTitle(title) //푸시알림의 제목
                        .setContentText(message) //푸시알림의 내용
                        .setChannelId(channel)
                        .setAutoCancel(true) //선택시 자동으로 삭제되도록 설정.
                        .setContentIntent(pendingIntent) //알림을 눌렀을때 실행할 인텐트 설정.
                        .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                }

                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    notificationManager.notify(9999, notificationBuilder?.build())
            }

        }
    }
}