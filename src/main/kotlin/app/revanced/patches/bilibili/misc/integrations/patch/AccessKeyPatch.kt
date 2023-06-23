package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.BiliAuthFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("access-key")
@Description("查找getAccessKey方法")
class AccessKeyPatch : BytecodePatch(listOf(BiliAuthFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        BiliAuthFingerprint.result?.mutableMethod?.run {
            val (getMethodRef, inst) = implementation!!.instructions.firstNotNullOfOrNull { inst ->
                if (inst is BuilderInstruction35c && inst.opcode == Opcode.INVOKE_STATIC) {
                    inst.reference.let {
                        if (it is MethodReference && it.returnType == it.definingClass
                            && it.parameterTypes == listOf("Landroid/content/Context;")
                        ) it to inst else null
                    }
                } else null
            } ?: return PatchResultError("not found get biliAccounts method")
            val getAccessKeyMethodRef = (implementation!!.instructions.indexOf(inst) + 2).let {
                (implementation!!.instructions[it] as BuilderInstruction35c).reference as MethodReference
            }
            val utilsClass = context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!
            val utilsGetContextMethod = utilsClass.mutableClass.methods.first { it.name == "getContext" }
            utilsClass.mutableClass.methods.run {
                first { it.name == "getAccessKey" }.also { remove(it) }
                    .cloneMutable(registerCount = 1, clearImplementation = true).apply {
                        addInstructions(
                            """
                            invoke-static {}, $utilsGetContextMethod
                            move-result-object v0
                            invoke-static {v0}, $getMethodRef
                            move-result-object v0
                            invoke-virtual {v0}, $getAccessKeyMethodRef
                            move-result-object v0
                            return-object v0
                        """.trimIndent()
                        )
                    }.also { add(it) }
            }
        } ?: return BiliAuthFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
