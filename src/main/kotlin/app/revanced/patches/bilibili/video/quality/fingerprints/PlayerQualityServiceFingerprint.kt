package app.revanced.patches.bilibili.video.quality.fingerprints

import app.revanced.patches.bilibili.patcher.fingerprint.MultiMethodFingerprint

object PlayerQualityServiceFingerprint : MultiMethodFingerprint(
    returnType = "I",
    strings = listOf("player.unite_login_qn", "player.unite_unlogin_qn")
)
