package com.axiel7.moelist.uicompose.airingwidget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.getValueSync
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.nextAiringDayFormatted
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.MainActivity
import com.axiel7.moelist.uicompose.theme.AppWidgetColumn
import com.axiel7.moelist.uicompose.theme.stringResource

class AiringWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val animeList = getAiringAnime(context)

        provideContent {
            GlanceTheme {
                if (animeList.isNullOrEmpty()) {
                    AppWidgetColumn(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.nothing_today),
                            modifier = GlanceModifier.padding(bottom = 8.dp),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }//: Column
                } else {
                    AppWidgetColumn {
                        LazyColumn {
                            items(animeList) { item ->
                                Column(
                                    modifier = GlanceModifier
                                        .padding(bottom = 8.dp)
                                        .fillMaxWidth()
                                        .clickable(actionStartActivity(
                                            Intent(LocalContext.current, MainActivity::class.java).apply {
                                                action = "details"
                                                putExtra("media_id", item.id)
                                                putExtra("media_type", "anime")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                addCategory(item.id.toString())
                                            }
                                        ))
                                ) {
                                    Text(
                                        text = item.title,
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.broadcast!!.nextAiringDayFormatted()
                                            ?: stringResource(R.string.unknown),
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onPrimaryContainer
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }//: LazyColumn
                    }//: Column
                }
            }
        }
    }
}

suspend fun getAiringAnime(context: Context): List<AnimeNode>? {
    return try {
        val token = context.defaultPreferencesDataStore
            .getValueSync(PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY)!!
        val nsfw = context.defaultPreferencesDataStore
            .getValueSync(PreferencesDataStore.NSFW_PREFERENCE_KEY) ?: false
        AnimeRepository.getAiringAnimeOnList(token = token, nsfw = nsfw)!!
    } catch (e: Exception) {
        null
    }
}

class AiringWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AiringWidget()
}