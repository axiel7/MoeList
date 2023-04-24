package com.axiel7.moelist.uicompose.base

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class StringArrayNavType : NavType<Array<String>>(isNullableAllowed = false) {
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