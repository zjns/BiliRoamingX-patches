package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.integrations.fingerprints.AppCompatActivityFingerprint

@Patch(
    name = "Dpi",
    description = "自定义dpi辅助补丁",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object DpiPatch : BytecodePatch(setOf(AppCompatActivityFingerprint)) {
    override fun execute(context: BytecodeContext) {
        AppCompatActivityFingerprint.result?.mutableClass?.methods?.run {
            first { it.name == "onConfigurationChanged" }.addInstruction(
                0, """
                invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onActivityPreConfigurationChanged(Landroid/app/Activity;Landroid/content/res/Configuration;)V
            """.trimIndent()
            )
            first { it.name == "attachBaseContext" }.addInstructions(
                0, """
                invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onActivityPreAttachBaseContext(Landroid/app/Activity;Landroid/content/Context;)Landroid/content/Context;
                move-result-object p1
            """.trimIndent()
            )
        } ?: throw AppCompatActivityFingerprint.exception
    }
}
