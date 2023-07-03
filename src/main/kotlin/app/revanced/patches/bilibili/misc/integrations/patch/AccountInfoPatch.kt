package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.DownloadingActivityFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("account-info")
@Description("获取账号信息辅助补丁")
class AccountInfoPatch : BytecodePatch(listOf(DownloadingActivityFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        DownloadingActivityFingerprint.result?.method?.implementation?.instructions?.firstNotNullOfOrNull { inst ->
            if (inst.opcode == Opcode.INVOKE_VIRTUAL && inst is Instruction35c) {
                (inst.reference as MethodReference).takeIf {
                    it.parameterTypes.isEmpty() && it.returnType == "Z"
                }
            } else null
        }?.let { vipMethodRef ->
            val accountInfoClass = context.classes.first { it.type == vipMethodRef.definingClass }
            val getMethod = accountInfoClass.methods.first { m ->
                m.accessFlags.let { AccessFlags.STATIC.isSet(it) && !AccessFlags.SYNTHETIC.isSet(it) }
                        && m.returnType == accountInfoClass.type && m.parameterTypes.isEmpty()
            }
            context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!.mutableClass.methods.run {
                first { it.name == "isEffectiveVip" }.also { remove(it) }.cloneMutable(
                    registerCount = 1, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        invoke-static {}, $getMethod
                        move-result-object v0
                        invoke-virtual {v0}, $vipMethodRef
                        move-result v0
                        return v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: return DownloadingActivityFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
