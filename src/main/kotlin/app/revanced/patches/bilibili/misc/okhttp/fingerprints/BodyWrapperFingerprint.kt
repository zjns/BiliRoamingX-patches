package app.revanced.patches.bilibili.misc.okhttp.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object BiliCallBodyWrapperFingerprint : MethodFingerprint(
    strings = listOf("Cannot read raw response body of a converted body."),
    customFingerprint = { _, classDef ->
        classDef.type.startsWith("Lcom/bilibili/okretro")
    }
)

object RetrofitBodyWrapperFingerprint : MethodFingerprint(
    strings = listOf("Cannot read raw response body of a converted body."),
    customFingerprint = { _, classDef ->
        classDef.type.startsWith("Lretrofit")
    }
)
