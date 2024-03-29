package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(
    name = "Integrations",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ],
    requiresIntegrations = true
)
object IntegrationsPatch : BaseIntegrationsPatch(
    "Lapp/revanced/bilibili/utils/Utils;",
    setOf(InitFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)
        val result = InitFingerprint.result!!
        result.mutableMethod.addInstruction(
            1, """
            invoke-static {p0}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onCreate(Landroid/app/Application;)V
        """.trimIndent()
        )
        result.mutableClass.methods.first {
            it.name == "attachBaseContext" && it.parameterTypes == listOf("Landroid/content/Context;")
        }.addInstructions(
            0, """
            invoke-static {p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->attachBaseContext(Landroid/content/Context;)Landroid/content/Context;
            move-result-object p1
        """.trimIndent()
        )
    }
}
