package com.axiel7.moelist.ui.base

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json

object StringArrayNavType : NavType<Array<String>>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Array<String>? {
        return bundle.getStringArray(key)
    }

    override fun parseValue(value: String): Array<String> {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: Array<String>) {
        bundle.putStringArray(key, value)
    }

}