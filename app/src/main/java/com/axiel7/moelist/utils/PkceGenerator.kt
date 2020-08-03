package com.axiel7.moelist.utils

import android.util.Base64
import org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom

object PkceGenerator {
    private const val CODE_VERIFIER_STRING =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._~"

    fun generateVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun generateVerifier(length: Int): String {
        return RandomStringUtils.random(length, CODE_VERIFIER_STRING)
    }
}