package app.revanced.patches.bilibili.ads.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@Name("up-rcmd-ads")
@BiliBiliCompatibility
@Description("移除UP主推荐广告")
class UpRcmdAdsPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bapis/bilibili/ad/v1/SourceContentDto;")?.let {
            it.mutableClass.methods.find { it.name == "getAdContent" }?.addInstructionsWithLabels(
                0, """
                invoke-static {}, Lapp/revanced/bilibili/patches/UpRcmdAdsPatch;->blockUpRcmdAds()Z
                move-result v0
                if-eqz v0, :jump
                const/4 v0, 0x0
                return-object v0
                :jump
                nop
                """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
