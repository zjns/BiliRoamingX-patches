package app.revanced.patches.bilibili.misc.protobuf.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MossMiddlewareGaiaFingerprint : MethodFingerprint(
    strings = listOf("moss.middleware.gaia", "Call onVoucher for")
)
