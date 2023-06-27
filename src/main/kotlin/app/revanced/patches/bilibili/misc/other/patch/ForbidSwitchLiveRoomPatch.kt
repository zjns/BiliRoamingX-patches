package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
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
@Name("forbid-switch-live-room")
@Description("禁止上下滑动切换直播间")
@Version("0.0.1")
class ForbidSwitchLiveRoomPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bilibili/bililive/room/ui/roomv3/vertical/widget/LiveVerticalPagerView;")?.run {
            immutableClass.fields.firstNotNullOfOrNull { f ->
                context.findClass(f.type)?.let { c ->
                    if (c.immutableClass.superclass == "Landroidx/recyclerview/widget/RecyclerView;") c
                    else null
                }
            }?.mutableClass?.methods?.find { it.name == "onInterceptTouchEvent" }?.addInstructionsWithLabels(
                0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/ForbidSwitchLiveRoomPatch;->forbid()Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x0
                    return v0
                    :jump
                    nop
                    """.trimIndent()
            )
        } ?: return PatchResultError("not found LivePagerRecyclerView class")
        return PatchResultSuccess()
    }
}
