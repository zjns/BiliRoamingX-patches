package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("force-old-player")
@Description("强制旧版播放器")
@Version("0.0.1")
class ForceOldPlayerPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Ltv/danmaku/biliplayerv2/GeminiPlayerFFKt;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.returnType == "Z" && it.parameters.isEmpty() }?.addInstructions(
                0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/ForceOldPlayerPatch;->forceOldPlayer()Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x0
                    return v0
                    :jump
                    nop
                """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
