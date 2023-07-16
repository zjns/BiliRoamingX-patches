package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DefaultSpeedFingerprint : MethodFingerprint(
    strings = listOf("player_key_video_speed"),
    parameters = listOf("Z"),
    returnType = "F",
    customFingerprint = { _, classDef ->
        classDef == SeekToFingerprint.result?.classDef
    }
)
