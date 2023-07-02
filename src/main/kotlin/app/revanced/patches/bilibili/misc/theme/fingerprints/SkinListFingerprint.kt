package app.revanced.patches.bilibili.misc.theme.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SkinListFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Ltv/danmaku/bili/ui/theme/api/BiliSkinList;", "Z"),
    customFingerprint = { _, classDef ->
        classDef.type == "Ltv/danmaku/bili/ui/theme/ThemeStoreActivity;"
    }
)
