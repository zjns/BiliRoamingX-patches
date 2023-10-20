package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AppCompatActivityFingerprint : MethodFingerprint(
    strings = listOf("androidx:appcompat"),
    parameters = listOf(),
    returnType = "V"
)
