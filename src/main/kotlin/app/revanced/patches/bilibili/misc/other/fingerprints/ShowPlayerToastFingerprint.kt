package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShowPlayerToastFingerprint : MethodFingerprint(
    strings = listOf("extra_title", "player show toast:"),
    parameters = listOf("Ltv/danmaku/biliplayerv2/widget/toast/PlayerToast;"),
    returnType = "V"
)
