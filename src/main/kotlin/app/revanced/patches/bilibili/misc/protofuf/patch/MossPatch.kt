package app.revanced.patches.bilibili.misc.protofuf.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.protofuf.fingerprints.MossServiceFingerprint
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("moss-patch")
@Description("gRPC 通信引擎服务 hook")
class MossPatch : BytecodePatch(listOf(MossServiceFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        MossServiceFingerprint.result?.mutableClass?.methods?.let { methods ->
            methods.find { it.name == "blockingUnaryCall" }?.run {
                addInstructions(
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
                val afterInsertIndex = implementation?.instructions?.indexOfLast {
                    it.opcode == Opcode.RETURN_OBJECT
                }?.takeIf { it != -1 } ?: return@run
                addInstructions(
                    afterInsertIndex, """
                    invoke-static {p2, p1}, Lapp/revanced/bilibili/patches/protobuf/MossPatch;->hookBlockingAfter(Lcom/google/protobuf/GeneratedMessageLite;Lcom/google/protobuf/GeneratedMessageLite;)Lcom/google/protobuf/GeneratedMessageLite;
                    move-result-object p1
                    # return-object p1
                """.trimIndent()
                )
            }
            methods.find { it.name == "asyncUnaryCall" }?.run {
                addInstructions(
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
