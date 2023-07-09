package app.revanced.patches.bilibili.misc.settings.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliHdCompatibility
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliHdCompatibility
@Name("hd-preference-fragment")
@Description("HD版本设置辅助补丁")
class HdPreferenceFragmentPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        val clearInst = context.findClass("Ltv/danmaku/bilibilihd/ui/main/preference/HdPreferenceFragment;")
            ?.immutableClass?.methods?.find { it.name == "onDestroyView" }?.implementation?.instructions?.findLast {
                it.opcode == Opcode.INVOKE_VIRTUAL && it is Instruction35c
            } ?: return PatchResultError("not found clear instruction")
        ((clearInst as Instruction35c).reference as MethodReference).let { m ->
            context.findClass(m.definingClass)!!.mutableClass.methods.first { it.name == m.name && it.parameterTypes == m.parameterTypes }
        }.run {
            val deleteIndex = implementation!!.instructions.indexOfLast { it.opcode == Opcode.SPUT_OBJECT }
            removeInstruction(deleteIndex)
        }
        return PatchResultSuccess()
    }
}
