package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.layout.fingerprints.FullStoryWidgetFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("disable-full-story")
@Description("禁用看一看按钮")
@Version("0.0.1")
class FullStoryWidgetPatch(
    private val fingerprints: List<FullStoryWidgetFingerprint> = listOf(
        FullStoryWidgetFingerprint("PlayerFullStoryWidget"),
        FullStoryWidgetFingerprint("GeminiPlayerFullStoryWidget")
    )
) : BytecodePatch(fingerprints) {
    override fun execute(context: BytecodeContext): PatchResult {
        fingerprints.mapNotNull { item ->
            item.result?.mutableClass?.let { clazz ->
                clazz.methods.find {
                    it.returnType == "Z" && it.parameters.size == 1 && it.parameters[0].type == clazz.type
                }?.run {
                    ((implementation?.instructions?.find {
                        it.opcode == Opcode.INVOKE_DIRECT
                    } as? BuilderInstruction35c)?.reference as? MethodReference)?.let { r ->
                        clazz.methods.find {
                            it.name == r.name && it.returnType == r.returnType && it.parameterTypes == r.parameterTypes
                        }
                    }
                }
            }
        }.forEach {
            it.addInstructions(
                0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/DisableStoryFullPatch;->disableStoryFull()Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x0
                    return v0
                    :jump
                    nop
                 """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
