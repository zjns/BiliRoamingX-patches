package app.revanced.patches.bilibili.layout.patch

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
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("remove-vip-section")
@Description("移除我的页面大会员横幅")
class RemoveVipSectionPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        val homeUserCenterFragmentClass = context.findClass("Ltv/danmaku/bili/ui/main2/mine/HomeUserCenterFragment;")
            ?: context.findClass("Ltv/danmaku/bilibilihd/ui/main/mine/HdHomeUserCenterFragment;")
            ?: return PatchResultError("not found HomeUserCenterFragment")
        homeUserCenterFragmentClass.mutableClass.let { clazz ->
            val viewField = clazz.fields.find {
                it.type == "Ltv/danmaku/bili/ui/main2/mine/widgets/MineVipEntranceView;"
            } ?: return PatchResultError("not found field of type MineVipEntranceView")
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
        return PatchResultSuccess()
    }
}
