package com.axiel7.moelist.utils

import org.apache.commons.lang3.RandomStringUtils

object PkceGenerator {
    private const val CODE_VERIFIER_STRING =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._~"

    fun generateVerifier(length: Int): String {
        return RandomStringUtils.random(length, CODE_VERIFIER_STRING)
    }
}