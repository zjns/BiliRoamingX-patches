package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.patch.BiliAccountsPatch
import app.revanced.patches.bilibili.misc.other.fingerprints.MineBindAccountStateFingerprint
import app.revanced.patches.bilibili.misc.other.fingerprints.SpaceBindAccountStateFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable

@Patch
@BiliBiliCompatibility
@DependsOn([BiliAccountsPatch::class])
@Name("number-format")
@Description("数字格式化补丁")
class NumberFormatPatch : BytecodePatch(
    listOf(MineBindAccountStateFingerprint, SpaceBindAccountStateFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val numerFormatPatchClass = context.classes.first {
            it.type == "Lapp/revanced/bilibili/patches/NumberFormatPatch;"
        }
        val mineBindMethod = numerFormatPatchClass.methods.first { it.name == "onMineBindAccountState" }
        val spaceBindMethod = numerFormatPatchClass.methods.first { it.name == "onSpaceBindAccountState" }
        MineBindAccountStateFingerprint.result?.run {
            mutableMethod.cloneMutable(registerCount = 3, clearImplementation = true).apply {
                mutableMethod.name += "_Origin"
                addInstructions(
                    """
                    invoke-direct {p0, p1, p2}, $mutableMethod
                    invoke-static {p0, p1}, $mineBindMethod
                    return-void
                """.trimIndent()
                )
            }.also { mutableClass.methods.add(it) }
        } ?: return MineBindAccountStateFingerprint.toErrorResult()
        SpaceBindAccountStateFingerprint.result?.run {
            mutableMethod.cloneMutable(registerCount = 2, clearImplementation = true).apply {
                mutableMethod.name += "_Origin"
                addInstructions(
                    """
                    invoke-direct {p0, p1}, $mutableMethod
                    invoke-static {p0, p1}, $spaceBindMethod
                    return-void
                """.trimIndent()
                )
            }.also { mutableClass.methods.add(it) }
        } ?: return SpaceBindAccountStateFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
