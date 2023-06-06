package app.revanced.patches.bilibili.misc.protobuf.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MossServiceFingerprint : MethodFingerprint(
    strings = listOf("moss.service", "MossService start to build engine.")
)
