package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.integrations.fingerprints.AppCompatActivityFingerprint

@Patch(
    name = "Dpi",
    description = "自定义dpi辅助补丁",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object DpiPatch : BytecodePatch(setOf(AppCompatActivityFingerprint)) {
    override fun execute(context: BytecodeContext) {
        AppCompatActivityFingerprint.result?.mutableClass?.methods?.first {
            it.name == "onConfigurationChanged"
        }?.addInstruction(
            0, """
            invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onActivityPreConfigurationChanged(Landroid/app/Activity;Landroid/content/res/Configuration;)V
        """.trimIndent()
        ) ?: throw AppCompatActivityFingerprint.exception
    }
}
