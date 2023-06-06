package app.revanced.patches.bilibili.layout.patch

import app.revanced.extensions.toErrorResult
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
import app.revanced.patches.bilibili.layout.fingerprints.HomeUserCenterFragmentFingerprint
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("remove-vip-section")
@Description("移除我的页面大会员横幅")
@Version("0.0.1")
class RemoveVipSectionPatch : BytecodePatch(listOf(HomeUserCenterFragmentFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        HomeUserCenterFragmentFingerprint.result?.mutableClass?.let { clazz ->
            val viewField = clazz.fields.find {
                it.type == "Ltv/danmaku/bili/ui/main2/mine/widgets/MineVipEntranceView;"
            } ?: return@let null
            val method = clazz.methods.find { it.name == "onCreateView" } ?: return@let null
            val insertIndex = method.implementation?.instructions?.indexOfLast {
                it.opcode == Opcode.RETURN_OBJECT
            }?.takeIf { it != -1 } ?: return@let null
            method.addInstructions(
                insertIndex, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/RemoveVipSectionPatch;->removeVipSection()Z
                    move-result v0
                    if-eqz v0, :jump
                    iget-object v0, p0, ${clazz.type}->${viewField.name}:${viewField.type}
                    if-eqz v0, :jump
                    const/16 p2, 0x8
                    invoke-virtual {v0, p2}, Landroid/widget/FrameLayout;->setVisibility(I)V
                    const/4 p2, 0x0
                    iput-object p2, p0, ${clazz.type}->${viewField.name}:${viewField.type}
                    :jump
                    nop
            """.trimIndent()
            )
        } ?: return HomeUserCenterFragmentFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
