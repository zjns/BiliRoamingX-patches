package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.AttachChannelInfoFingerprint
import app.revanced.util.exception

@Patch(
    name = "Upgrade",
    description = "自定义更新辅助补丁",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object UpgradePatch : BytecodePatch(setOf(AttachChannelInfoFingerprint)) {
    override fun execute(context: BytecodeContext) {
        AttachChannelInfoFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
            invoke-static {}, Lapp/revanced/bilibili/patches/UpgradePatch;->customUpdate()Z
            move-result v0
            if-eqz v0, :jump
            return-void
            :jump
            nop
        """.trimIndent()
        ) ?: throw AttachChannelInfoFingerprint.exception
    }
}
