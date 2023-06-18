package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("disable-teenager-mode-dialog")
@Description("禁用青少年模式弹窗")
@Version("0.0.1")
class TeenagerModePatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bilibili/teenagersmode/ui/TeenagersModeDialogActivity;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "onCreate" }?.addInstructionsWithLabels(
                0, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/TeenagerModePatch;->disableDialog(Landroid/app/Activity;)Z
                    move-result v0
                    if-eqz v0, :jump
                    return-void
                    :jump
                    nop
            """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
