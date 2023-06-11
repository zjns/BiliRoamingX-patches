package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.SectionFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("auto-like")
@Description("视频自动点赞补丁")
@Version("0.0.1")
class AutoLikePatch : BytecodePatch(listOf(SectionFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val clazz = SectionFingerprint.result?.mutableClass
            ?: return SectionFingerprint.toErrorResult()
        val likeMethod = context.findClass(clazz.superclass!!)!!.immutableClass.virtualMethods.find { m ->
            m.parameterTypes.size == 1 && m.returnType == "V" && !AccessFlags.FINAL.isSet(m.accessFlags)
        } ?: return PatchResultError("can not found like method")
        val realLikeMethod = clazz.methods.first { m ->
            m.name == likeMethod.name && m.parameterTypes == likeMethod.parameterTypes
        }
        val insertIndex = realLikeMethod.implementation!!.instructions
            .indexOfLast { it.opcode == Opcode.RETURN_VOID }
        realLikeMethod.addInstruction(
            insertIndex, """
            invoke-static {p0}, Lapp/revanced/bilibili/patches/AutoLikePatch;->autoLike(Ljava/lang/Object;)V
        """.trimIndent()
        )
        return PatchResultSuccess()
    }
}
