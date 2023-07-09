package app.revanced.patches.bilibili.misc.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PegasusParserFingerprint : MethodFingerprint(
    strings = listOf("items", "config"),
    returnType = "Lcom/bilibili/okretro/GeneralResponse;"
)
