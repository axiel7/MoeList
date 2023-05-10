package com.axiel7.moelist.uicompose.airingwidget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.getValueSync
import com.axiel7.moelist.data.repository.AnimeRepository
import java.time.Duration

class AiringWidgetWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(AiringWidget::class.java)
        return try {
            // Update state to indicate loading
            setWidgetState(glanceIds, AiringInfo.Loading)

            // Update state with new data
            val token = context.defaultPreferencesDataStore.getValueSync(ACCESS_TOKEN_PREFERENCE_KEY)!!
            val nsfw = context.defaultPreferencesDataStore.getValueSync(NSFW_PREFERENCE_KEY) ?: false
            setWidgetState(glanceIds, AiringInfo.Available(
                animeList = AnimeRepository.getAiringAnimeOnList(token = token, nsfw = nsfw)!!
            ))

            Result.success()
        } catch (e: Exception) {
            setWidgetState(glanceIds, AiringInfo.Unavailable(e.message.orEmpty()))
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: AiringInfo) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = AiringInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
        }
        AiringWidget().updateAll(context)
    }

    companion object {
        private const val uniqueWorkName = "AiringWidgetWorker"

        fun enqueue(context: Context, force: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<AiringWidgetWorker>(
                Duration.ofHours(12)
            )
            var workPolicy = ExistingPeriodicWorkPolicy.KEEP

            // Replace any enqueued work and expedite the request
            if (force) {
                workPolicy = ExistingPeriodicWorkPolicy.UPDATE
            }

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }
}