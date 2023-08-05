package app.revanced.patches.bilibili.video.player.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object FFUniteDetailAbFingerprint : MethodFingerprint(
    strings = listOf("ff_unite_detail2", "key_sp_video_detail_united_detail2"),
    returnType = "Z",
    parameters = listOf()
)
