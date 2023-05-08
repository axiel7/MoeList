package com.axiel7.moelist.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.model.media.numeric
import com.axiel7.moelist.uicompose.MainActivity
import com.axiel7.moelist.utils.DateUtils.getNextDayOfWeek
import com.axiel7.moelist.data.datastore.PreferencesDataStore.notificationsDataStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (isStopped) return Result.success()

        val animeTitle = inputData.getString("anime_title")
        val animeId = inputData.getInt("anime_id", 1)

        val resultPendingIntent = TaskStackBuilder.create(applicationContext).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = "details"
                    putExtra("media_id", animeId)
                    putExtra("media_type", "anime")
                }
            )
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(applicationContext, AIRING_ANIME_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.airing))
            .setContentText(animeTitle ?: "")
            .setSmallIcon(R.drawable.ic_moelist_logo_white)
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
            notify(animeId, builder.build())
        }

        return Result.success()
    }

    companion object {

        suspend fun scheduleAiringAnimeNotification(
            context: Context,
            title: String,
            animeId: Int,
            weekDay: WeekDay,
            jpHour: LocalTime,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createAiringAnimeNotificationChannel(context)
            }

            val airingDay = LocalDate.now().getNextDayOfWeek(DayOfWeek.of(weekDay.numeric()))
            val startDateTime = LocalDateTime.of(airingDay, jpHour)
                .atZone(SeasonCalendar.japanZoneId)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()

            val delay = startDateTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

            val inputData = Data.Builder()
                .putInt("anime_id", animeId)
                .putString("anime_title", title)
                .build()

            val workManager = WorkManager.getInstance(context)

            //store notification setting
            context.notificationsDataStore.edit {
                it[stringPreferencesKey(animeId.toString())] = title
            }

            val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork("notification_$animeId", ExistingPeriodicWorkPolicy.UPDATE, notificationWorkRequest)
        }

        suspend fun removeAiringAnimeNotification(context: Context, animeId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork("notification_$animeId")
            context.notificationsDataStore.edit {
                it.remove(stringPreferencesKey(animeId.toString()))
            }
        }

        suspend fun removeAllNotifications(context: Context) {
            WorkManager.getInstance(context).cancelAllWork()
            context.notificationsDataStore.edit {
                it.clear()
            }
        }
    }
}

const val AIRING_ANIME_CHANNEL_ID = "airing_notifications"

@RequiresApi(Build.VERSION_CODES.O)
fun createAiringAnimeNotificationChannel(context: Context) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(AIRING_ANIME_CHANNEL_ID, context.getString(R.string.airing), importance)
    channel.description = ""
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
