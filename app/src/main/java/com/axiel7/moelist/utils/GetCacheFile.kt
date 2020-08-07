package com.axiel7.moelist.utils

import android.content.Context
import okhttp3.Cache

object GetCacheFile {
    fun getCacheFile(context: Context, mibSize: Long) : Cache? {
        val cacheSize :Long = mibSize * 1024 * 1024 // 20MiB
        return context.cacheDir?.let { Cache(it, cacheSize) }
    }
}