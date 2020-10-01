package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selectedUrl = ""

    private val downloadQuery = DownloadManager.Query()

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        customButton.setOnClickListener {
            if (download_list.checkedRadioButtonId != -1) {
                when (download_list.checkedRadioButtonId) {
                    R.id.load_app_download_option -> {
                        selectedUrl = UDACITY_URL
                    }
                    R.id.retrofit_download_option -> {
                        selectedUrl = RETROFIT_URL
                    }
                    R.id.glide_download_option -> {
                        selectedUrl = GLIDE_URL
                    }
                }
                customButton.setState(ButtonState.Loading)
                download()
            } else {
                Toast.makeText(
                    this@MainActivity, getString(R.string.please_select_option),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        createChannel(
            getString(R.string.load_app_notification_channel_id),
            getString(R.string.load_app_notification_channel_name)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val cursor = downloadManager.query(downloadQuery)
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            customButton.setState(ButtonState.Completed)
                            sendDownloadNotification(getString(R.string.failed_to_download))
                        }
                        DownloadManager.STATUS_PENDING -> {
                        }
                        DownloadManager.STATUS_PAUSED -> {
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            customButton.setState(ButtonState.Completed)
                            sendDownloadNotification(getString(R.string.download_complete))
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            customButton.setState(ButtonState.Loading)
                            notificationManager.showNotificationProgress()
                        }
                    }
                }
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getFileNameFromUrl(selectedUrl))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)

        downloadQuery.setFilterById(downloadID)
    }

    private fun sendDownloadNotification(status: String) {
        notificationManager = ContextCompat.getSystemService(
            this@MainActivity,
            NotificationManager::class.java
        ) as NotificationManager

        with(notificationManager) {
            cancelNotifications()
            sendNotification(selectedUrl, status, this@MainActivity)
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            with(notificationChannel) {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(false)
                description = getString(R.string.load_app_notification_description)
            }

            notificationManager = getSystemService(NotificationManager::class.java)!!.apply {
                createNotificationChannel(notificationChannel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
    }
}
