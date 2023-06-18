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
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@BiliBiliCompatibility
@Name("disable-live-room-double-click")
@Description("禁用直播间双击点赞")
@Version("0.0.1")
class LiveRoomPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bilibili/bililive/room/ui/roomv3/player/container/LiveRoomPlayerContainerView;")
            ?.mutableClass?.run {
                methods.find { it.name == "onDoubleTap" }?.addInstructionsWithLabels(
                    0, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/LiveRoomPatch;->onDoubleTap(${this.type})Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x1
                    return v0
                    :jump
                    nop
                """.trimIndent()
                )
                val info = methods.firstNotNullOfOrNull { m ->
                    val index = m.implementation?.instructions?.indexOfFirst {
                        it is Instruction35c && it.reference.toString() ==
                                "Landroid/os/SystemClock;->elapsedRealtime()J"
                    }
                    if (index != null && index != -1) m to index + 2 else null
                }
                if (info != null) {
                    val (m, insertIndex) = info
                    m.addInstructionsWithLabels(
                        insertIndex, """
                        invoke-static {}, Lapp/revanced/bilibili/patches/LiveRoomPatch;->disableLiveRoomDoubleClick()Z
                        move-result v3
                        if-eqz v3, :jump
                        return-void
                        :jump
                        nop
                    """.trimIndent()
                    )
                }
            } ?: return PatchResultError("not found LiveRoomPlayerContainerView class")
        return PatchResultSuccess()
    }
}
