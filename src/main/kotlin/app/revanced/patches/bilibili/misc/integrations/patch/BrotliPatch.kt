package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.BrotliFingerprint

@Patch
@BiliBiliCompatibility
@Name("brotli")
@Description("集成Brotli")
class BrotliPatch : BytecodePatch(listOf(BrotliFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        BrotliFingerprint.result?.classDef?.run {
            val brotliClass = context.findClass("Lapp/revanced/bilibili/api/BrotliInputStream;")!!
            brotliClass.mutableClass.setSuperClass(this.type)
            brotliClass.mutableClass.methods.first { it.name == "<init>" }.replaceInstruction(
                0, """
                invoke-direct {p0, p1}, ${this.type}-><init>(Ljava/io/InputStream;)V
            """.trimIndent()
            )
        } ?: return BrotliFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
