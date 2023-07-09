package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch

@Patch
@Name("integrations")
@BiliBiliCompatibility
@RequiresIntegrations
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/bilibili/utils/Utils;",
    listOf(InitFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = super.execute(context)
        if (result is PatchResultSuccess) {
            InitFingerprint.result?.mutableMethod?.addInstruction(
                1, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onCreate(Landroid/app/Application;)V
            """.trimIndent()
            )
        }
        return result
    }
}
