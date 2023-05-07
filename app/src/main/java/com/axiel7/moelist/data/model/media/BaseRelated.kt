package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

abstract class BaseRelated {
    abstract val node: BaseMediaNode
    abstract val relationType: String
    abstract val relationTypeFormatted: String
}

@Composable
fun BaseRelated.relationLocalized() = when (this.relationTypeFormatted) {
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
    else -> this.relationTypeFormatted
}