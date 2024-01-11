package app.revanced.patches.bilibili.misc.config.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ConfigSourceFingerprint : MethodFingerprint(
    strings = listOf("AES/CBC/PKCS7Padding"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "<clinit>"
    }
)
