package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.QualityViewHolderFingerprint
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.toErrorResult

@Patch
@BiliBiliCompatibility
@Name("trial-quality")
@Description("试用画质辅助补丁")
class TrialQualityPatch : MultiMethodBytecodePatch(
    multiFingerprints = listOf(QualityViewHolderFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        super.execute(context)
        val patchMethod = context.findClass("Lapp/revanced/bilibili/patches/TrialQualityPatch;")!!
            .mutableClass.methods.first { it.name == "onBindOnline" }
        QualityViewHolderFingerprint.result.associate { r ->
            r.mutableClass.methods to r.mutableClass.methods.first { m ->
                m.parameterTypes.let { it.size == 5 && it[1] == "Z" && it[3] == "Landroid/widget/TextView;" && it[4] == "Landroid/widget/TextView;" }
            }
        }.ifEmpty {
            return QualityViewHolderFingerprint.toErrorResult()
        }.forEach { (methods, method) ->
            val originMethod = method.cloneMutable(name = method.name + "_Origin")
                .also { methods.add(it) }
            method.also { methods.remove(it) }.cloneMutable(registerCount = 6, clearImplementation = true).apply {
                addInstructions(
                    """
                    invoke-direct/range {p0 .. p5}, $originMethod
                    invoke-static {p2, p4, p5}, $patchMethod
                    return-void
                """.trimIndent()
                )
            }.also { methods.add(it) }
        }
        return PatchResultSuccess()
    }
}
