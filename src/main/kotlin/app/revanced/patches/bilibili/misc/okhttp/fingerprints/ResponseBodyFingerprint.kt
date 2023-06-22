package app.revanced.patches.bilibili.misc.okhttp.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ResponseBodyFingerprint : MethodFingerprint(
    strings = listOf("Cannot buffer entire body for content length: ")
)
