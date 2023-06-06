package app.revanced.patches.bilibili.video.quality.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerQualityServiceFingerprint : MethodFingerprint(
    returnType = "I",
    strings = listOf("player.unite_login_qn", "player.unite_unlogin_qn")
)
