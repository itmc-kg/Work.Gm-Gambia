package kg.jobs.app.notification

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kg.jobs.app.BuildConfig
import kg.jobs.app.R
import kg.jobs.app.ui.SplashActivity


class NotificationCenter(val context: Context,
                         val notificationManager: NotificationManagerCompat) {

    private var mainIntent = Intent(context, SplashActivity::class.java)

    fun showMessageNotification(title: String,
                                content: String,
                                chatId: String) {
        val channelId = BuildConfig.APPLICATION_ID + "/messages"
        val channelName = context.getString(R.string.notification_channel_message_name)
        val notificationBuilder = getNotificationBuilder(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.drawable.ic_push)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#6C419B"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(getPendingIntent(mainIntent.putExtra("chatId", chatId)))

        if (isSystemSoundEnable()) {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
        notificationBuilder.setGroup("message_group").setGroupSummary(true)
        notificationManager.notify(BuildConfig.APPLICATION_ID, chatId.hashCode(), notificationBuilder.build())
    }

    fun showApplicationNotification(title: String,
                                    content: String,
                                    applicationId: String) {
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra("applicationId", applicationId)

        val channelId = BuildConfig.APPLICATION_ID + "/application"
        val channelName = context.getString(R.string.notification_channel_application)
        val notification = getNotificationBuilder(channelId, channelName)
                .setSmallIcon(R.drawable.ic_push)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(content)
                .setColor(Color.parseColor("#6C419B"))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent(intent))
                .build()
        notificationManager.notify(BuildConfig.APPLICATION_ID, applicationId.hashCode(), notification)
    }

    fun showOtherNotification(title: String,
                              content: String) {
        val intent = Intent(context, SplashActivity::class.java)

        val channelId = BuildConfig.APPLICATION_ID + "/other"
        val channelName = context.getString(R.string.notification_channel_other)
        val notification = getNotificationBuilder(channelId, channelName)
                .setSmallIcon(R.drawable.ic_push)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(content)
                .setColor(Color.parseColor("#6C419B"))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent(intent))
                .build()
        notificationManager.notify(BuildConfig.APPLICATION_ID, (title + content).hashCode(), notification)
    }


    fun dismissNotification(id: Int) {
        notificationManager.cancel(BuildConfig.APPLICATION_ID, id)
    }

    fun dismissAllNotification() {
        notificationManager.cancelAll()
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    private fun getNotificationBuilder(channelId: String,
                                       channelName: String,
                                       importance: Int = NotificationManager.IMPORTANCE_DEFAULT): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, importance)
            manager.createNotificationChannel(channel)
            NotificationCompat.Builder(context, channel.id)
        } else {
            NotificationCompat.Builder(context)
        }
    }

    private fun getPendingIntent(intent: Intent): PendingIntent {
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun isSystemSoundEnable(): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audio.getStreamVolume(AudioManager.STREAM_RING) > 0
    }
}