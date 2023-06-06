package app.revanced.patches.bilibili.video.quality.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object PlayerPreloadHolderFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf(
        "Ltv/danmaku/bili/videopage/common/preload/PreloadType;",
        "Ljava/lang/String;"
    ),
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC
    )
)
