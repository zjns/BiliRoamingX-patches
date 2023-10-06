package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Remove vip section",
    description = "移除我的页面大会员横幅",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili")]
)
object RemoveVipSectionPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        val homeUserCenterFragmentClass = context.findClass("Ltv/danmaku/bili/ui/main2/mine/HomeUserCenterFragment;")
            ?: throw PatchException("not found HomeUserCenterFragment")
        homeUserCenterFragmentClass.mutableClass.let { clazz ->
            val viewField = clazz.fields.find {
                it.type == "Ltv/danmaku/bili/ui/main2/mine/widgets/MineVipEntranceView;"
            } ?: throw PatchException("not found field of type MineVipEntranceView")
            val method = clazz.methods.first { it.name == "onCreateView" }
            val insertIndex = method.implementation!!.instructions.indexOfLast { it.opcode == Opcode.RETURN_OBJECT }
            method.addInstructionsWithLabels(
                insertIndex, """
                invoke-static {}, Lapp/revanced/bilibili/patches/RemoveVipSectionPatch;->removeVipSection()Z
                move-result v0
                if-eqz v0, :jump
                iget-object v0, p0, $viewField
                if-eqz v0, :jump
                const/16 p2, 0x8
                invoke-virtual {v0, p2}, Landroid/widget/FrameLayout;->setVisibility(I)V
                const/4 p2, 0x0
                iput-object p2, p0, $viewField
                :jump
                nop
            """.trimIndent()
            )
        }
    }
}
