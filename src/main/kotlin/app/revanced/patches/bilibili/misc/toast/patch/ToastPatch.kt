package app.revanced.patches.bilibili.misc.toast.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.toast.fingerprints.LessonsModeToastFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("toast-call-patch")
@Description("调用APP自带Toast补丁")
@Version("0.0.1")
class ToastPatch : BytecodePatch(listOf(LessonsModeToastFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val showToastRef = LessonsModeToastFingerprint.result?.mutableMethod
            ?.implementation?.instructions?.firstNotNullOfOrNull { s ->
                if (s.opcode != Opcode.INVOKE_STATIC)
                    return@firstNotNullOfOrNull null
                ((s as Instruction35c).reference as MethodReference).let { mr ->
                    if (mr.returnType == "V" && mr.parameterTypes.let {
                            it.size == 3 && it[0] == "Landroid/content/Context;" && it[1] == "Ljava/lang/String;" && it[2] == "I"
                        }) mr else null
                }
            } ?: return LessonsModeToastFingerprint.toErrorResult()
        val myToastsClass = context.findClass("Lapp/revanced/bilibili/utils/Toasts;")!!
        val cancelMethod = context.findClass(showToastRef.definingClass)!!.immutableClass.methods.find {
            it.parameterTypes.isEmpty() && it.name != "<init>"
        } ?: return PatchResultError("can not found cancel toast method")
        myToastsClass.mutableClass.methods.run {
            first { it.name == "show" && it.parameterTypes.size == 3 }.also { remove(it) }
                .cloneMutable(3, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {p0, p1, p2}, $showToastRef
                        return-void
                        """.trimIndent()
                    )
                }.also { add(it) }
            first { it.name == "cancel" }.also { remove(it) }
                .cloneMutable(0, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {}, $cancelMethod
                        return-void
                    """.trimIndent()
                    )
                }.also { add(it) }
        }
        return PatchResultSuccess()
    }
}
