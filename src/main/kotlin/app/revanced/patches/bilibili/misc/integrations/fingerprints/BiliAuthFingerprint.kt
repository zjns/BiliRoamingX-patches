package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object BiliAuthFingerprint : MethodFingerprint(
    strings = listOf("initFacial enter")
)
