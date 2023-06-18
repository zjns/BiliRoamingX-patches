package app.revanced.patches.bilibili.misc.protobuf.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.protobuf.fingerprints.MossServiceFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@BiliBiliCompatibility
@Name("moss-patch")
@Description("gRPC 通信引擎服务 hook")
class MossPatch : BytecodePatch(listOf(MossServiceFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        MossServiceFingerprint.result?.mutableClass?.methods?.let { methods ->
            methods.find { it.name == "blockingUnaryCall" }?.run {
                val implementation = implementation ?: return@run
                addInstructionsWithLabels(
                    0, """
                    invoke-static {p2}, Lapp/revanced/bilibili/patches/protobuf/MossPatch;->hookBlockingBefore(Lcom/google/protobuf/GeneratedMessageLite;)Ljava/lang/Object;
                    move-result-object v0
                    if-eqz v0, :jump
                    sget-object v1, Lapp/revanced/bilibili/meta/HookFlags;->STOP_EXECUTION:Ljava/lang/Object;
                    if-eq v0, v1, :stop_execution
                    goto :modify_result
                    :stop_execution
                    const/4 v0, 0x0
                    return-object v0
                    :modify_result
                    check-cast v0, Lcom/google/protobuf/GeneratedMessageLite;
                    return-object v0
                    :jump
                    nop
                """.trimIndent()
                )
                val invokeInst = implementation.instructions.findLast {
                    it.opcode == Opcode.INVOKE_INTERFACE
                } as Instruction35c
                removeInstructions(implementation.instructions.size - 3, 3)
                addInstructionsWithLabels(
                    implementation.instructions.size,
                    """
                    const/4 v1, 0x0
                    const/4 v2, 0x0
                    #:try_start
                    invoke-interface {v0, p1, p2, p3}, ${invokeInst.reference}
                    move-result-object p1
                    #:try_end
                    #.catch Lcom/bilibili/lib/moss/api/MossException; {:try_start .. :try_end} :catch
                    move-object v1, p1
                    goto :modify_result

                    #:catch
                    move-exception p1
                    move-object v2, p1

                    :modify_result
                    invoke-static {p2, v1, v2}, Lapp/revanced/bilibili/patches/protobuf/MossPatch;->hookBlockingAfter(Lcom/google/protobuf/GeneratedMessageLite;Lcom/google/protobuf/GeneratedMessageLite;Lcom/bilibili/lib/moss/api/MossException;)Lcom/google/protobuf/GeneratedMessageLite;

                    move-result-object p1
                    return-object p1
                """.trimIndent()
                )
                val invokeIndex = implementation.instructions.indexOfLast {
                    it.opcode == Opcode.INVOKE_INTERFACE
                }
                val moveExceptionIndex = implementation.instructions.indexOfLast {
                    it.opcode == Opcode.MOVE_EXCEPTION
                }
                implementation.addCatch(
                    "Lcom/bilibili/lib/moss/api/MossException;",
                    implementation.newLabelForIndex(invokeIndex),
                    implementation.newLabelForIndex(invokeIndex + 2),
                    implementation.newLabelForIndex(moveExceptionIndex)
                )
            }
            methods.find { it.name == "asyncUnaryCall" }?.run {
                addInstructionsWithLabels(
                    0, """
                    invoke-static {p2, p3}, Lapp/revanced/bilibili/patches/protobuf/MossPatch;->hookAsyncBefore(Lcom/google/protobuf/GeneratedMessageLite;Lcom/bilibili/lib/moss/api/MossResponseHandler;)Ljava/lang/Object;
                    move-result-object v0
                    if-eqz v0, :jump
                    sget-object v1, Lapp/revanced/bilibili/meta/HookFlags;->STOP_EXECUTION:Ljava/lang/Object;
                    if-eq v0, v1, :stop_execution
                    goto :modify_handler
                    :stop_execution
                    return-void
                    :modify_handler
                    check-cast v0, Lcom/bilibili/lib/moss/api/MossResponseHandler;
                    move-object p3, v0
                    :jump
                    nop
                """.trimIndent()
                )
            }
        } ?: return MossServiceFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
