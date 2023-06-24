package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.other.fingerprints.OgvSearchResultFingerprint
import app.revanced.patches.bilibili.utils.*
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@BiliBiliCompatibility
@Name("append-extra-search-type")
@Description("附加更多搜索类型补丁")
class AppendExtraSearchTypePatch : BytecodePatch(listOf(OgvSearchResultFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val pagerTypesClass =
            context.findClass("Lcom/bilibili/search/result/pages/BiliMainSearchResultPage\$PageTypes;")
                ?: return PatchResultError("not found pager type class")
        pagerTypesClass.mutableClass.run {
            methods.first { it.name == "<init>" && it.parameterTypes.size == 5 }.run {
                accessFlags = accessFlags.toPublic()
            }
            fields.first { it.name == "\$VALUES" }.run {
                accessFlags = accessFlags.toPublic().removeFinal()
            }
        }
        OgvSearchResultFingerprint.result?.run {
            val typeFiled = mutableMethod.run {
                implementation!!.instructions.firstNotNullOf {
                    if (it is BuilderInstruction22c && it.opcode == Opcode.IGET) {
                        it.reference as FieldReference
                    } else null
                }
            }
            method(
                definingClass = mutableClass.type,
                name = "getTypeForBiliRoaming",
                returnType = "I",
                accessFlags = AccessFlags.PUBLIC.value,
                implementation = methodImplementation(2)
            ).toMutable().apply {
                addInstructions(
                    """
                    iget v0, p0, $typeFiled
                    return v0
                """.trimIndent()
                )
            }.also { mutableClass.methods.add(it) }
            method(
                definingClass = mutableClass.type,
                name = "setTypeForBiliRoaming",
                returnType = "V",
                parameters = listOf(methodParameter(type = "I", name = "type")),
                accessFlags = AccessFlags.PUBLIC.value,
                implementation = methodImplementation(2)
            ).toMutable().apply {
                addInstructions(
                    """
                    iput p1, p0, $typeFiled
                    return-void
                """.trimIndent()
                )
            }.also { mutableClass.methods.add(it) }
            mutableClass.methods.first { it.name == "setUserVisibleCompat" }.addInstruction(
                0, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/okhttp/BangumiSeasonHook;->onOgvSearchResultFragmentVisible(Lcom/bilibili/search/ogv/OgvSearchResultFragment;)V
                """.trimIndent()
            )
        } ?: return OgvSearchResultFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
