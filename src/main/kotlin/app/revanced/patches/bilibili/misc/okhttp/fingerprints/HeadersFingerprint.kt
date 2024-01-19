package app.revanced.patches.bilibili.misc.okhttp.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object HeadersFingerprint : MethodFingerprint(
    strings = listOf("Expected alternating header names and values")
)
