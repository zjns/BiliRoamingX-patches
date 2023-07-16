package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerServiceFingerprint : MethodFingerprint(
    strings = listOf("class=%s is not core service")
)
