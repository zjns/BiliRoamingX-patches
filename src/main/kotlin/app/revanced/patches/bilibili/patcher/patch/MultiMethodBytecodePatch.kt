package app.revanced.patches.bilibili.patcher.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.bilibili.patcher.fingerprint.MultiMethodFingerprint
import app.revanced.patches.bilibili.patcher.fingerprint.MultiMethodFingerprint.Companion.resolve

abstract class MultiMethodBytecodePatch(
    val fingerprints: Iterable<MethodFingerprint>? = null,
    val multiFingerprints: Iterable<MultiMethodFingerprint>? = null
) : BytecodePatch(fingerprints) {
    override fun execute(context: BytecodeContext): PatchResult {
        multiFingerprints?.resolve(context, context.classes)
        return PatchResultSuccess()
    }
}
