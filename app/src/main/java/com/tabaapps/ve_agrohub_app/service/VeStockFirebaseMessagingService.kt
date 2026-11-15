package com.tabaapps.ve_agrohub_app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tabaapps.ve_agrohub_app.R

/**
 * Firebase Cloud Messaging (FCM) Service for ve-stock notifications.
 * Handles Low Stock Alerts and Stock Refill Broadcast notifications.
 *
 * Java Educational Note:
 * In Java Android development, FCM messaging services extend `com.google.firebase.messaging.FirebaseMessagingService`.
 * Method `onMessageReceived(RemoteMessage remoteMessage)` is triggered asynchronously when a push notification payload arrives.
 * `NotificationManager` handles Android system status bar alerts using `NotificationCompat.Builder`.
 */
class VeStockFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "ve-stock Inventory Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Stock update received for VE AGRO HUB."

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refreshed - could send token to backend / Firestore
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "ve_stock_alerts_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ve-stock Low Stock & Refill Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for stock alerts and inventory refills"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_ve_agro_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
