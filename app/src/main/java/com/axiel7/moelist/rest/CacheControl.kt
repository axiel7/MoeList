package com.axiel7.moelist.rest

import android.content.Context
import com.axiel7.moelist.utils.NetworkState.hasNetwork
import okhttp3.Interceptor
import okhttp3.Response

class CacheControl(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = if (hasNetwork(context)!!)
            /*
            *  If there is Internet, get the cache that was stored 1 min. ago
            *  If the cache is older than 5 seconds, then discard it,
            *  and indicate an error in fetching the response.
            *  The 'max-age' attribute is responsible for this behavior.
            */
             request.newBuilder()
                 .header("Cache-Control", "public, max-age=" + 60).build()
        else
            /*
            *  If there is no Internet, get the cache that was stored 7 days ago.
            *  If the cache is older than 7 days, then discard it,
            *  and indicate an error in fetching the response.
            *  The 'max-stale' attribute is responsible for this behavior.
            *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
            */
            request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}").build()

        return chain.proceed(request)
    }
}