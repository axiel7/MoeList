package com.axiel7.moelist.worker

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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.main.MainActivity
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.getNextDayOfWeek
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val animeRepository: AnimeRepository,
    private val notificationWorkerManager: NotificationWorkerManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (isStopped) return Result.success()

        val animeTitle = inputData.getString("anime_title")
        val animeId = inputData.getInt("anime_id", 1)

        inputData.getString("type")?.let { type ->
            if (type == "start")
                notificationWorkerManager.removeStartAiringAnimeNotification(animeId)
        }

        // remove periodic worker if anime ended
        val animeDetails = animeRepository.getAnimeAiringStatus(animeId)
        if (animeDetails?.status != MediaStatus.AIRING) {
            notificationWorkerManager.removeAiringAnimeNotification(animeId)
            return Result.success()
        }

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
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(applicationContext, AIRING_ANIME_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.airing))
            .setContentText(animeTitle ?: "")
            .setSmallIcon(R.drawable.ic_moelist_logo_white)
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
            notify(animeId, builder.build())
        }

        return Result.success()
    }
}

class NotificationWorkerManager(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {
    private val workManager get() = WorkManager.getInstance(context)

    suspend fun scheduleAiringAnimeNotification(
        title: String,
        animeId: Int,
        weekDay: WeekDay,
        jpHour: LocalTime,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAiringAnimeNotificationChannel(context)
        }

        val airingDay = LocalDate.now().getNextDayOfWeek(DayOfWeek.of(weekDay.numeric))
        val startDateTime = LocalDateTime.of(airingDay, jpHour)
            .atZone(SeasonCalendar.japanZoneId)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        val delay = startDateTime.toEpochSecond(DateUtils.defaultZoneOffset) -
                LocalDateTime.now().toEpochSecond(DateUtils.defaultZoneOffset)

        val inputData = Data.Builder()
            .putInt("anime_id", animeId)
            .putString("anime_title", title)
            .build()

        //store notification setting
        dataStore.edit {
            it[stringPreferencesKey(animeId.toString())] = title
        }

        val notificationWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "notification_$animeId",
            ExistingPeriodicWorkPolicy.UPDATE,
            notificationWorkRequest
        )
    }

    suspend fun removeAiringAnimeNotification(animeId: Int) {
        workManager.cancelUniqueWork("notification_$animeId")
        dataStore.edit {
            it.remove(stringPreferencesKey(animeId.toString()))
        }
    }

    suspend fun removeStartAiringAnimeNotification(animeId: Int) {
        dataStore.edit {
            it.remove(stringPreferencesKey("start_$animeId"))
        }
    }

    suspend fun scheduleAnimeStartNotification(
        title: String,
        animeId: Int,
        startDate: LocalDate,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAiringAnimeNotificationChannel(context)
        }

        val delay = startDate.atStartOfDay().toEpochSecond(DateUtils.defaultZoneOffset) -
                LocalDateTime.now().toEpochSecond(DateUtils.defaultZoneOffset)

        val inputData = Data.Builder()
            .putInt("anime_id", animeId)
            .putString("anime_title", title)
            .putString("type", "start")
            .build()

        //store start notification setting
        dataStore.edit {
            it[stringPreferencesKey("start_$animeId")] = title
        }

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            "notification_start_$animeId",
            ExistingWorkPolicy.REPLACE,
            notificationWorkRequest
        )
    }

    suspend fun removeAllNotifications() {
        workManager.cancelAllWork()
        dataStore.edit {
            it.clear()
        }
    }

    fun getNotification(mediaId: Int) = dataStore.data.map {
        it[stringPreferencesKey(mediaId.toString())]
    }

    fun getStartNotification(mediaId: Int) = dataStore.data.map {
        it[stringPreferencesKey("start_$mediaId")]
    }
}

const val AIRING_ANIME_CHANNEL_ID = "airing_notifications"

@RequiresApi(Build.VERSION_CODES.O)
fun createAiringAnimeNotificationChannel(context: Context) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel =
        NotificationChannel(AIRING_ANIME_CHANNEL_ID, context.getString(R.string.airing), importance)
    channel.description = ""
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
