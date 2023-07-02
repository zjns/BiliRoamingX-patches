package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("channel-tab")
@Description("底栏添加频道辅助补丁")
class ChannelTabUIPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bilibili/pegasus/channelv2/home/category/HomeCategoryFragment;")
            ?.mutableClass?.methods?.find { it.name == "onViewCreated" }?.addInstructions(
                0, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/ChannelTabUIPatch;->onHomeCategoryFragmentViewCreated(Landroidx/fragment/app/Fragment;)V
            """.trimIndent()
            )
        return PatchResultSuccess()
    }
}
