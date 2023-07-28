package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShareToFingerprint : MethodFingerprint(
    strings = listOf("share.helper.inner"),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)
