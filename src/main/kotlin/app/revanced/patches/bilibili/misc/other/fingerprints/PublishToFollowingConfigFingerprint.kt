package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PublishToFollowingConfigFingerprint : MethodFingerprint(
    strings = listOf("PublishToFollowingConfig(visible=")
)
