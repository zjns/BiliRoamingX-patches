package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MusicNotificationHelperFingerprint : MethodFingerprint(
    strings = listOf("buildNewJBNotification"),
    returnType = "Landroid/app/Notification;",
    parameters = listOf("Landroid/graphics/Bitmap;")
)
