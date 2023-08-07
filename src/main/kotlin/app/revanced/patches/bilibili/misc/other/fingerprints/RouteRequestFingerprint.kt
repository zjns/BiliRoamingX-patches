package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RouteRequestFingerprint : MethodFingerprint(
    strings = listOf("RouteRequest(targetUri=")
)
