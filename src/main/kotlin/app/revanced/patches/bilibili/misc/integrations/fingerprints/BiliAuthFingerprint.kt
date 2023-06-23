package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object BiliAuthFingerprint : MethodFingerprint(
    strings = listOf("initFacial enter")
)
