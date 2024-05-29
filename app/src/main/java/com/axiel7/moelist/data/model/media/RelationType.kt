package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RelationType : Localizable {
    @SerialName("prequel")
    PREQUEL,

    @SerialName("sequel")
    SEQUEL,

    @SerialName("summary")
    SUMMARY,

    @SerialName("alternative_version")
    ALTERNATIVE_VERSION,

    @SerialName("alternative_setting")
    ALTERNATIVE_SETTING,

    @SerialName("spin_off")
    SPIN_OFF,

    @SerialName("side_story")
    SIDE_STORY,

    @SerialName("parent_story")
    PARENT_STORY,

    @SerialName("full_story")
    FULL_STORY,

    @SerialName("adaptation")
    ADAPTATION,

    @SerialName("character")
    CHARACTER,

    @SerialName("other")
    OTHER;

    @Composable
    override fun localized() = when (this) {
        PREQUEL -> stringResource(R.string.relation_prequel)
        SEQUEL -> stringResource(R.string.relation_sequel)
        SUMMARY -> stringResource(R.string.relation_summary)
        ALTERNATIVE_VERSION -> stringResource(R.string.relation_alternative_version)
        ALTERNATIVE_SETTING -> stringResource(R.string.relation_alternative_setting)
        SPIN_OFF -> stringResource(R.string.relation_spin_off)
        SIDE_STORY -> stringResource(R.string.relation_side_story)
        PARENT_STORY -> stringResource(R.string.parent_story)
        FULL_STORY -> stringResource(R.string.full_story)
        ADAPTATION -> stringResource(R.string.adaptation)
        CHARACTER -> stringResource(R.string.relation_character)
        OTHER -> stringResource(R.string.relation_other)
    }
}