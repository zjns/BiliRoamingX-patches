package app.revanced.patches.bilibili.misc.config.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ABSourceFingerprint : MethodFingerprint(
    strings = listOf("key: ", ", invoke"),
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/Boolean;"),
    returnType = "Ljava/lang/Boolean;",
)
