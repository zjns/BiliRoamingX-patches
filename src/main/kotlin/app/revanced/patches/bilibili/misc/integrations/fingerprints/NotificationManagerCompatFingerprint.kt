package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object NotificationManagerCompatFingerprint : MethodFingerprint(
    strings = listOf("ff_notification_cancelall_intercept"),
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "V"
)
