package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MusicWrapperPlayerFingerprint : MethodFingerprint(
    strings = listOf("call playNextVideo")
)
