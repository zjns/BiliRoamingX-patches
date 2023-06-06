package app.revanced.patches.bilibili.layout.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object HomeUserCenterFragmentFingerprint : MethodFingerprint(
    strings = listOf("activity://main/preference", "uploader")
)
