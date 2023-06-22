package app.revanced.patches.bilibili.misc.okhttp.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RequestFingerprint : MethodFingerprint(strings = listOf("Request{method="))
