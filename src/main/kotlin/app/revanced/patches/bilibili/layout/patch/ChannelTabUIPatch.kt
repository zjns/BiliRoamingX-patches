package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch

@Patch(
    name = "Channel tab",
    description = "底栏添加频道辅助补丁",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object ChannelTabUIPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        context.findClass("Lcom/bilibili/pegasus/channelv2/home/category/HomeCategoryFragment;")
            ?.mutableClass?.methods?.find { it.name == "onViewCreated" }?.addInstructions(
                0, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/ChannelTabUIPatch;->onHomeCategoryFragmentViewCreated(Landroidx/fragment/app/Fragment;)V
            """.trimIndent()
            )
    }
}
