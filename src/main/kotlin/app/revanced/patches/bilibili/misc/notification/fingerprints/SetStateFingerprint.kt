package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SetStateFingerprint : MethodFingerprint(
    strings = listOf("MediaSession setPlaybackState"),
    parameters = listOf("I"),
    returnType = "V"
)
