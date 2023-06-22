package app.revanced.patches.bilibili.misc.okhttp.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MediaTypeGetFingerprint : MethodFingerprint(strings = listOf("No subtype found for:"))
