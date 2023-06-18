package app.revanced.patches.bilibili.misc.protobuf.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("main-page-story")
@Description("首页左上角头像点击禁止跳转短视频补丁")
class MainPageStoryPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bapis/bilibili/app/distribution/setting/experimental/MultipleTusConfig;")
            ?.mutableClass?.methods?.first { it.name == "getTopLeft" }?.run {
                val insertIndex = implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.IF_NEZ
                }
                addInstruction(
                    insertIndex, """
                    invoke-static {v0}, Lapp/revanced/bilibili/patches/DisableMainPageStoryPatch;->disableMainPageStory(Lcom/bapis/bilibili/app/distribution/setting/experimental/TopLeft;)V
                """.trimIndent()
                )
            }
        return PatchResultSuccess()
    }
}
