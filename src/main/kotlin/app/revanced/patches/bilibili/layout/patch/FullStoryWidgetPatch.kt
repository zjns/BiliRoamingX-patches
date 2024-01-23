package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.layout.fingerprints.GeminiPlayerFullStoryWidgetFingerprint
import app.revanced.patches.bilibili.layout.fingerprints.PlayerFullStoryWidgetFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Disable full story",
    description = "禁用看一看按钮",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd"), CompatiblePackage(name = "com.bilibili.app.in")]
)
object FullStoryWidgetPatch : BytecodePatch(
    setOf(PlayerFullStoryWidgetFingerprint, GeminiPlayerFullStoryWidgetFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        arrayOf(PlayerFullStoryWidgetFingerprint, GeminiPlayerFullStoryWidgetFingerprint).mapNotNull { item ->
            item.result?.mutableClass?.let { clazz ->
                clazz.methods.find {
                    it.returnType == "Z" && it.parameters.size == 1 && it.parameters[0].type == clazz.type
                }?.run {
                    ((implementation?.instructions?.find {
                        it.opcode == Opcode.INVOKE_DIRECT
                    } as? Instruction35c)?.reference as? MethodReference)?.let { r ->
                        clazz.methods.find {
                            it.name == r.name && it.returnType == r.returnType && it.parameterTypes == r.parameterTypes
                        }
                    }
                }
            }
        }.forEach {
            it.addInstructionsWithLabels(
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
    }
}
