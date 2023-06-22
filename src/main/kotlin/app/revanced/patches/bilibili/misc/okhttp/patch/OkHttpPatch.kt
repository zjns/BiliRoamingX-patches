package app.revanced.patches.bilibili.misc.okhttp.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.okhttp.fingerprints.*
import app.revanced.patches.bilibili.utils.toPublic
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("okhttp")
@Description("OkHttp网络请求响应Hook")
class OkHttpPatch : BytecodePatch(
    listOf(
        HttpUrlFingerprint,
        MediaTypeGetFingerprint,
        RequestFingerprint,
        ResponseBodyFingerprint,
        ResponseFingerprint,
        BiliCallBodyWrapperFingerprint,
        RetrofitBodyWrapperFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val httpUrlClass = HttpUrlFingerprint.result?.classDef
            ?: return HttpUrlFingerprint.toErrorResult()
        val requestClass = RequestFingerprint.result?.classDef
            ?: return RequestFingerprint.toErrorResult()
        val urlField = requestClass.fields.first { it.type == httpUrlClass.type }
        val responseClass = ResponseFingerprint.result?.mutableClass
            ?: return ResponseFingerprint.toErrorResult()
        responseClass.fields.forEach { it.accessFlags = it.accessFlags and AccessFlags.FINAL.value.inv() }
        val requestField = responseClass.fields.first { it.type == requestClass.type }
        val codeField = responseClass.fields.first { it.type == "I" }
        val responseBodyClass = ResponseBodyFingerprint.result?.classDef
            ?: return ResponseBodyFingerprint.toErrorResult()
        val responseBodyField = responseClass.fields.first { it.type == responseBodyClass.type }
        val mediaTypeGetMethod = MediaTypeGetFingerprint.result?.method
            ?: return MediaTypeGetFingerprint.toErrorResult()
        val mediaTypeType = mediaTypeGetMethod.definingClass
        val createMethod = responseBodyClass.methods.first { m ->
            AccessFlags.STATIC.isSet(m.accessFlags) && m.parameterTypes.let { ts ->
                ts.size == 2 && ts[0] == mediaTypeType && ts[1] == "Ljava/lang/String;"
            }
        }
        val stringMethod = responseBodyClass.methods.first { m ->
            m.returnType == "Ljava/lang/String;" && m.parameterTypes.isEmpty()
        }
        val biliCallBodyWrapperClass = BiliCallBodyWrapperFingerprint.result?.mutableClass
            ?: return BiliCallBodyWrapperFingerprint.toErrorResult()
        val retrofitBodyWrapperClass = RetrofitBodyWrapperFingerprint.result?.mutableClass
            ?: return RetrofitBodyWrapperFingerprint.toErrorResult()
        biliCallBodyWrapperClass.run { accessFlags = accessFlags.toPublic() }
        retrofitBodyWrapperClass.run { accessFlags = accessFlags.toPublic() }
        responseClass.methods.first { it.name == "<init>" }.run {
            val insertIndex = implementation!!.instructions.indexOfLast {
                it.opcode == Opcode.RETURN_VOID
            }
            addInstructionsWithLabels(
                insertIndex, """
                iget-object p1, p0, $responseBodyField
                if-eqz p1, :jump
                instance-of v0, p1, $biliCallBodyWrapperClass
                if-nez v0, :jump
                instance-of v0, p1, $retrofitBodyWrapperClass
                if-nez v0, :jump
                iget-object v0, p0, $requestField
                iget-object v1, v0, $urlField
                invoke-virtual {v1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                move-result-object v1
                iget v0, p0, $codeField
                invoke-static {v1, v0}, Lapp/revanced/bilibili/patches/okhttp/OkHttpPatch;->shouldHook(Ljava/lang/String;I)Z
                move-result p1
                if-eqz p1, :jump
                iget-object p1, p0, $responseBodyField
                invoke-virtual {p1}, $stringMethod
                move-result-object p1
                invoke-static {v1, v0, p1}, Lapp/revanced/bilibili/patches/okhttp/OkHttpPatch;->hook(Ljava/lang/String;ILjava/lang/String;)Ljava/lang/String;
                move-result-object v0
                const-string v1, "application/json; charset=utf-8"
                invoke-static {v1}, $mediaTypeGetMethod
                move-result-object v1
                invoke-static {v1, v0}, $createMethod
                move-result-object v0
                const/16 v1, 0xc8
                iput v1, p0, $codeField
                iput-object v0, p0, $responseBodyField
                :jump
                nop
            """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
