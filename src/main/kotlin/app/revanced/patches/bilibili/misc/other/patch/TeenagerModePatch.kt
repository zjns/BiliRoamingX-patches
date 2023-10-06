package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.bilibili.misc.other.fingerprints.TeenagerModeCheckFingerprint
import app.revanced.patches.bilibili.misc.other.fingerprints.TeenagerModeOnShowFingerprint

@Patch(
    name = "Teenager mode",
    description = "禁用青少年模式弹窗",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object TeenagerModePatch : BytecodePatch(
    setOf(TeenagerModeCheckFingerprint, TeenagerModeOnShowFingerprint)
) {
    override fun execute(context: BytecodeContext) {
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
    }
}
