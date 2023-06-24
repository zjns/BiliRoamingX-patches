package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.utils.cloneMutable

@Patch
@BiliBiliCompatibility
@Name("protobuf-print")
@Description("使 ProtoBuf toString 打印更具可读性")
class ProtoBufPrintPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        val toStringClass = context.findClass("Lcom/google/protobuf/MessageLiteToString;")!!
        val toStringExClass = context.findClass("Lcom/google/protobuf/MessageLiteToStringEx;")!!
        val exToStringMethod = toStringExClass.immutableClass.methods.first { it.name == "toString" }
        val exPrintFieldMethod = toStringExClass.immutableClass.methods.first { it.name == "printField" }
        toStringClass.mutableClass.methods.run {
            first { it.name == exToStringMethod.name }.also { remove(it) }
                .cloneMutable(2, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {p0, p1}, $exToStringMethod
                        move-result-object p0
                        return-object p0
                    """.trimIndent()
                    )
                }.also { add(it) }
            first { it.name == exPrintFieldMethod.name }.also { remove(it) }
                .cloneMutable(4, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {p0, p1, p2, p3}, $exPrintFieldMethod
                        return-void
                        """.trimIndent()
                    )
                }.also { add(it) }
        }
        return PatchResultSuccess()
    }
}
