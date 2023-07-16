package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object NotificationCompatBuilderFingerprint : MethodFingerprint(
    strings = listOf("android.people.list", "android.chronometerCountDown", "android.colorized")
)
