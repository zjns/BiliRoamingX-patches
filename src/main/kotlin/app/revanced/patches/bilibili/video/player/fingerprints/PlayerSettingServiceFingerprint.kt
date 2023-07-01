package app.revanced.patches.bilibili.video.player.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerSettingServiceFingerprint : MethodFingerprint(
    strings = listOf("PlayerSettingService", "could not remove all key for scope:")
)
