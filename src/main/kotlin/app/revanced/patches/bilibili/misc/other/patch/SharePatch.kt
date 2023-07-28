package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.AppendTrackingInfoFingerprint
import app.revanced.patches.bilibili.misc.other.fingerprints.ShareToFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("share-patch")
@Description("分享处理相关补丁")
class SharePatch : BytecodePatch(listOf(ShareToFingerprint, AppendTrackingInfoFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = ShareToFingerprint.result
        val shareToMethodRef = result?.method?.implementation?.instructions
            ?.firstNotNullOfOrNull { inst ->
                if (inst.opcode == Opcode.INVOKE_VIRTUAL && inst is Instruction35c) {
                    (inst.reference as MethodReference).let {
                        if (it.parameterTypes == listOf("Ljava/lang/String;", "Landroid/os/Bundle;")) it else null
                    }
                } else null
            } ?: return ShareToFingerprint.toErrorResult()
        result.mutableClass.run {
            findMutableMethodOf(shareToMethodRef).let { method ->
                method.cloneMutable(registerCount = 3, clearImplementation = true).apply {
                    method.name += "_Origin"
                    addInstructions(
                        """
                        invoke-static {p1, p2}, Lapp/revanced/bilibili/patches/SharePatch;->onShareTo(Ljava/lang/String;Landroid/os/Bundle;)V
                        invoke-virtual {p0, p1, p2}, $method
                        return-void
                    """.trimIndent()
                    )
                }.also {
                    methods.add(it)
                }
            }
        }
        AppendTrackingInfoFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
                invoke-static {}, Lapp/revanced/bilibili/patches/SharePatch;->disableAppendTrackingInfo()Z
                move-result v0
                if-eqz v0, :jump
                return-object p1
                :jump
                nop
            """.trimIndent()
        ) ?: return AppendTrackingInfoFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
