package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object TeenagerModeCheckFingerprint : MethodFingerprint(
    strings = listOf("special_mode_show_force_popup_window"),
    parameters = listOf("Landroid/app/Activity;"),
    returnType = "V"
)

object TeenagerModeOnShowFingerprint : MethodFingerprint(
    strings = listOf("bilibili://teenagers_mode/dialog"),
    parameters = listOf(),
    returnType = "V"
)
