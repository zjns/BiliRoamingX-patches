package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.MineBindAccountStateFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Bili accounts",
    description = "获取账户相关接口辅助补丁",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object BiliAccountsPatch : BytecodePatch(setOf(MineBindAccountStateFingerprint)) {
    override fun execute(context: BytecodeContext) {
        MineBindAccountStateFingerprint.result?.method?.implementation?.instructions?.run {
            val (biliAccountsClass, getMethodRef) = firstNotNullOfOrNull { inst ->
                if (inst.opcode == Opcode.INVOKE_STATIC) {
                    ((inst as Instruction35c).reference as MethodReference).takeIf {
                        it.parameterTypes == listOf("Landroid/content/Context;") && it.definingClass == it.returnType
                    }?.let { it.definingClass to it }
                } else null
            } ?: throw PatchException("not found BiliAccounts.get method")
            val isLoginMethodRef = firstNotNullOfOrNull { inst ->
                if (inst.opcode == Opcode.INVOKE_VIRTUAL) {
                    ((inst as Instruction35c).reference as MethodReference).takeIf {
                        it.definingClass == biliAccountsClass && it.parameterTypes.isEmpty() && it.returnType == "Z"
                    }
                } else null
            } ?: throw PatchException("not found BiliAccounts.isLogin method")
            val utilsClass = context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!.mutableClass
            val utilsGetContextMethod = utilsClass.methods.first { it.name == "getContext" }
            utilsClass.methods.run {
                first { it.name == "isLogin" }.also { remove(it) }.cloneMutable(
                    registerCount = 1, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        invoke-static {}, $utilsGetContextMethod
                        move-result-object v0
                        invoke-static {v0}, $getMethodRef
                        move-result-object v0
                        invoke-virtual {v0}, $isLoginMethodRef
                        move-result v0
                        return v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: throw MineBindAccountStateFingerprint.exception
    }
}
