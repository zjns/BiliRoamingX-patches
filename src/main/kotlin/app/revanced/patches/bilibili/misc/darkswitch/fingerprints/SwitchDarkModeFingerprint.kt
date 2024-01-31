package app.revanced.patches.bilibili.misc.darkswitch.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SwitchDarkModeFingerprint : MethodFingerprint(
    strings = listOf("default"),
    returnType = "V",
    parameters = listOf("Z"),
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("HomeUserCenterFragment;")
    }
)
