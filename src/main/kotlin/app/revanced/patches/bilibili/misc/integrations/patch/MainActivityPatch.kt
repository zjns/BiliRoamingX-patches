package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("main-activity-patch")
@Description("代理部分MainActivity方法补丁")
@Version("0.0.1")
class MainActivityPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Ltv/danmaku/bili/MainActivityV2;")?.mutableClass?.run {
            methods.find { it.name == "onCreate" }?.run {
                addInstruction(
                    implementation!!.instructions.size - 1,
                    """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/main/MainActivityDelegate;->onCreate(Ltv/danmaku/bili/MainActivityV2;)V
                """.trimIndent()
                )
            }
            /*ImmutableMethod(
                "Ltv/danmaku/bili/MainActivityV2;",
                "onPostCreate",
                listOf(ImmutableMethodParameter("Landroid/os/Bundle;", setOf(), null)),
                "V",
                AccessFlags.PROTECTED.value,
                null,
                null,
                ImmutableMethodImplementation(2, listOf(), null, null)
            ).toMutable().apply {
                addInstructions(
                    """
                    invoke-super {p0, p1}, $superclass->onPostCreate(Landroid/os/Bundle;)V
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/main/MainActivityDelegate;->onPostCreate(Ltv/danmaku/bili/MainActivityV2;)V
                    return-void
                """.trimIndent()
                )
            }.let { methods.add(it) }*/
            methods.find { it.name == "onStart" }?.run {
                val insertIdx = implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_SUPER } + 1
                addInstruction(
                    insertIdx, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/main/MainActivityDelegate;->onStart(Ltv/danmaku/bili/MainActivityV2;)V
                """.trimIndent()
                )
            }
            methods.find { it.name == "onBackPressed" }?.addInstructionsWithLabels(
                0,
                """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/main/MainActivityDelegate;->onBackPressed(Ltv/danmaku/bili/MainActivityV2;)Z
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
                """.trimIndent()
            )
        } ?: return PatchResultError("main activity patch failed")
        return PatchResultSuccess()
    }
}
