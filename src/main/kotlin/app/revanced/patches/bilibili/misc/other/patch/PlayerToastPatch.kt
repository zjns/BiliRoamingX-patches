package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.ShowPlayerToastFingerprint

@Patch(
    name = "Player toast",
    description = "播放器特定样式toast显示拦截",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd"), CompatiblePackage(name = "com.bilibili.app.in")]
)
object PlayerToastPatch : BytecodePatch(setOf(ShowPlayerToastFingerprint)) {
    override fun execute(context: BytecodeContext) {
        ShowPlayerToastFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/PlayerToastPatch;->onShow(Ltv/danmaku/biliplayerv2/widget/toast/PlayerToast;)Z
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
        ) ?: throw ShowPlayerToastFingerprint.exception
    }
}
