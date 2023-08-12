package app.revanced.patches.bilibili.video.player.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerOnPreparedFingerprint : MethodFingerprint(
    strings = listOf("PlayerCoreServiceV2", "[ijk][callback]player onPrepared")
)
