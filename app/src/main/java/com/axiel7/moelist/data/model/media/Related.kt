package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.utils.StringExtensions.formatRelation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Related(
    @SerialName("node")
    val node: AnimeNode,
    @SerialName("relation_type")
    val relationType: String = "",
    @SerialName("relation_type_formatted")
    val relationTypeFormatted: String = "",
)

fun Related.isManga(): Boolean = node.mediaType == "manga"
        || node.mediaType == "one_shot"
        || node.mediaType == "manhwa"
        || node.mediaType == "novel"
        || node.mediaType == "doujinshi"
        || node.mediaType == "light_novel"
        || node.mediaType == "manhua"

@Composable
fun String.relationLocalized() = when (this) {
    "Prequel" -> stringResource(R.string.relation_prequel)
    "Sequel" -> stringResource(R.string.relation_sequel)
    "Summary" -> stringResource(R.string.relation_summary)
    "Alternative version" -> stringResource(R.string.relation_alternative_version)
    "Alternative setting" -> stringResource(R.string.relation_alternative_setting)
    "Spin-off" -> stringResource(R.string.relation_spin_off)
    "Side story" -> stringResource(R.string.relation_side_story)
    "Parent story" -> stringResource(R.string.parent_story)
    "Full story" -> stringResource(R.string.full_story)
    "Adaptation" -> stringResource(R.string.adaptation)
    "Other" -> stringResource(R.string.relation_other)
    else -> this
}