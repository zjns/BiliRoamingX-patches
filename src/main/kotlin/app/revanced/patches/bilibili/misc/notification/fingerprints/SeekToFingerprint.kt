package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SeekToFingerprint : MethodFingerprint(
    strings = listOf("PlayerCoreServiceV2", "[player]seek to"),
    returnType = "V"
)
