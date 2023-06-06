package app.revanced.patches.bilibili.video.quality.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerSettingHelperFingerprint : MethodFingerprint(
    returnType = "I",
    strings = listOf("PlayerSettingHelper", "get free data failed")
)
