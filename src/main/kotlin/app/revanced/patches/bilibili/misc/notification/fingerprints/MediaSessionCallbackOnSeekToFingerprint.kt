package app.revanced.patches.bilibili.misc.notification.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MediaSessionCallbackOnSeekToFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        classDef.type.startsWith("Landroid/support/v4/media/session")
                && methodDef.name == "onSeekTo" && methodDef.parameterTypes == listOf("J")
    }
)
