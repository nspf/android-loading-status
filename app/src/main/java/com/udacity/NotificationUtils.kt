package com.udacity

/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder

// Notification ID.
private const val NOTIFICATION_ID = 0

// Intent keys
const val FILE_URL_KEY = "file_name"
const val STATUS_KEY = "status"
const val NOTIFICATION_ID_KEY = "notification_id"

lateinit var builder: NotificationCompat.Builder

/**
 * Builds and delivers the notification.
 */
fun NotificationManager.sendNotification(fileUrl: String, status: String, context: Context) {

    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(context, DetailActivity::class.java).apply {
        putExtra(FILE_URL_KEY, fileUrl)
        putExtra(STATUS_KEY, status)
        putExtra(NOTIFICATION_ID_KEY, NOTIFICATION_ID)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    val stackBuilder = TaskStackBuilder.create(context)
    with(stackBuilder) {
        addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
        addNextIntent(contentIntent)
    }

    val detailPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

    // Add style
    val downloadIcon = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.ic_cloud_download_outline_grey600_48dp
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(downloadIcon)
        .bigLargeIcon(null)

    // Build the notification
    builder = NotificationCompat.Builder(
        context,
        context.getString(R.string.load_app_notification_channel_id)
    )

        // set title, text and icon to builder
        .setSmallIcon(R.drawable.cloud_download_outline)
        .setContentTitle(getFileNameFromUrl(fileUrl))
        .setContentText(status)

        // set content intent
        .setContentIntent(detailPendingIntent)
        .setAutoCancel(true)

        // add style to builder
        .setStyle(bigPicStyle)
        .setLargeIcon(downloadIcon)

        // set priority
        .setPriority(NotificationCompat.PRIORITY_LOW)

        // Add an action button to open the Detail screen when tapped
        .addAction(R.drawable.ic_cloud_download_outline_black_18dp, "View details",
            detailPendingIntent)

        .setProgress(0, 0, false)

    // call notify
    notify(NOTIFICATION_ID, builder.build())
}

/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

fun NotificationManager.showNotificationProgress() {
    builder.setProgress(0, 0, true)
    builder.setContentText("Downloading...")
    notify(NOTIFICATION_ID, builder.build())

}