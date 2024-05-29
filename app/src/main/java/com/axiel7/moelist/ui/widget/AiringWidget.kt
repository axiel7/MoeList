package com.axiel7.moelist.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.main.MainActivity
import com.axiel7.moelist.ui.theme.AppWidgetColumn
import com.axiel7.moelist.ui.theme.glanceStringResource
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AiringWidget : GlanceAppWidget(), KoinComponent {

    private val defaultPreferencesRepository: DefaultPreferencesRepository by inject()
    private val animeRepository: AnimeRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        App.accessToken = defaultPreferencesRepository.accessToken.first()
        val titleLanguage = defaultPreferencesRepository.titleLang.first()
        val animeList = getAiringAnime()

        provideContent {
            GlanceTheme {
                if (animeList.isNullOrEmpty()) {
                    AppWidgetColumn(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = glanceStringResource(R.string.nothing_today),
                            modifier = GlanceModifier.padding(bottom = 8.dp),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                textAlign = TextAlign.Center
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
                                            Intent(
                                                LocalContext.current,
                                                MainActivity::class.java
                                            ).apply {
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
                                        text = item.title(titleLanguage),
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.broadcast!!.nextAiringDayFormatted()
                                            ?: glanceStringResource(R.string.unknown),
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

    private suspend fun getAiringAnime(): List<AnimeNode>? {
        return try {
            animeRepository.getAiringAnimeOnList()!!
        } catch (e: Exception) {
            null
        }
    }
}

class AiringWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AiringWidget()
}