package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object OnSeekCompleteFingerprint : MethodFingerprint(
    strings = listOf("[player]seek complete")
)
