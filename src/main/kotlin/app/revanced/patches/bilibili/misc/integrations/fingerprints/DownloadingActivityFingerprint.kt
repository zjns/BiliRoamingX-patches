package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DownloadingActivityFingerprint : MethodFingerprint(
    strings = listOf("meantime"),
    customFingerprint = { _, classDef ->
        classDef.type == "Ltv/danmaku/bili/ui/offline/DownloadingActivity;"
    }
)
