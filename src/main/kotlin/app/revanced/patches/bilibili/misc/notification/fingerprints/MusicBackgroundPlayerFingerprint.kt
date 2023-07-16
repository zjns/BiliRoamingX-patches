package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MusicBackgroundPlayerFingerprint : MethodFingerprint(
    strings = listOf("MusicBackgroundPlayBack status changed")
)
