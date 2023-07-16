package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object BackgroundPlayerFingerprint : MethodFingerprint(
    strings = listOf("backgroundPlayer status changed"),
    parameters = listOf("I", "Z"),
    returnType = "V"
)
