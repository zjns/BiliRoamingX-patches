package app.revanced.patches.bilibili.misc.copy.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DescCopyFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Z", "Ljava/lang/String;"),
    strings = listOf("clipboard", "text", "1", "2")
)
