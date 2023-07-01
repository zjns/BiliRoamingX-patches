package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.PublishToFollowingConfigFingerprint

@Patch
@BiliBiliCompatibility
@Name("disable_auto_select")
@Description("禁止自动转到动态")
class PublishToFollowingPatch : BytecodePatch(listOf(PublishToFollowingConfigFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PublishToFollowingConfigFingerprint.result?.mutableClass?.methods?.find { m ->
            m.name == "<init>" && m.parameterTypes.let { ts -> ts.size == 4 && ts.all { it == "Z" } }
        }?.addInstructions(
            0, """
            invoke-static {p3}, Lapp/revanced/bilibili/patches/PublishToFollowingPatch;->shouldAutoSelectOnce(Z)Z
            move-result p3
        """.trimIndent()
        ) ?: return PublishToFollowingConfigFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
