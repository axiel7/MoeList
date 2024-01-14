package com.axiel7.moelist.ui.base.navigation

enum class NavArgument(
    val type: ArgumentType,
) {
    MediaType(ArgumentType.String),
    MediaId(ArgumentType.Int),
    Pictures(ArgumentType.StringArray),
}