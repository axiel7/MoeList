package com.axiel7.moelist.ui.base.navigation

import androidx.navigation.NavType

enum class ArgumentType {
    String,
    StringArray,
    Int,
    Boolean,
    BooleanOptional,
    ;

    fun toNavType() = when (this) {
        String -> NavType.StringType
        StringArray -> StringArrayNavType
        Int -> NavType.IntType
        Boolean -> NavType.BoolType
        BooleanOptional -> NavType.StringType // null boolean is not supported in androidx.navigation
    }

    val isOptional
        get() = when (this) {
            BooleanOptional -> true
            else -> false
        }
}