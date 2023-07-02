package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object BLKVFingerprint : MethodFingerprint(
    strings = listOf(".blkv"),
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Z", "I")
)
