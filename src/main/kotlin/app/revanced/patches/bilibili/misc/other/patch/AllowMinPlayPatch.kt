package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("allow-mini-play")
@Description("允许解锁番剧小窗播放")
class AllowMinPlayPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        val playConfigType = "Lcom/bilibili/lib/media/resource/PlayConfig\$PlayConfigType;"
        context.findClass("Lcom/bilibili/lib/media/resource/PlayConfig\$PlayMenuConfig;")
            ?.mutableClass?.methods?.find {
                it.name == "<init>" && it.parameterTypes == listOf("Z", playConfigType)
            }?.addInstructionsWithLabels(
                0, """
                invoke-static {p2}, Lapp/revanced/bilibili/patches/AllowMiniPlayPatch;->allowMiniPlay($playConfigType)Z
                move-result v0
                if-eqz v0, :jump
                const/4 p1, 0x1
                :jump
                nop
            """.trimIndent()
            ) ?: return PatchResultError("not found PlayMenuConfig class")
        return PatchResultSuccess()
    }
}
