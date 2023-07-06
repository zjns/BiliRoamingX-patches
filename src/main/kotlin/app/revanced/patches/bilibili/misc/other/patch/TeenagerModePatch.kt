package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.TeenagerModeCheckFingerprint
import app.revanced.patches.bilibili.misc.other.fingerprints.TeenagerModeOnShowFingerprint

@Patch
@BiliBiliCompatibility
@Name("disable-teenager-mode-dialog")
@Description("禁用青少年模式弹窗")
@Version("0.0.1")
class TeenagerModePatch : BytecodePatch(
    listOf(TeenagerModeCheckFingerprint, TeenagerModeOnShowFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        fun MutableMethod.disable() {
            addInstructionsWithLabels(
                0, """
                invoke-static {}, Lapp/revanced/bilibili/patches/TeenagerModePatch;->shouldDisable()Z
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
            )
        }
        TeenagerModeCheckFingerprint.result?.mutableMethod?.disable()
        TeenagerModeOnShowFingerprint.result?.mutableMethod?.disable()
        return PatchResultSuccess()
    }
}
