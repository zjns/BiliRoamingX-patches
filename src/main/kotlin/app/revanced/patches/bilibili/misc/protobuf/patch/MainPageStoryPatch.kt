package app.revanced.patches.bilibili.misc.protobuf.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Main page story",
    description = "首页左上角头像点击禁止跳转短视频补丁",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object MainPageStoryPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
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
    }
}
