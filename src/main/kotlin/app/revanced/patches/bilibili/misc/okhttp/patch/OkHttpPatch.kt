package app.revanced.patches.bilibili.misc.okhttp.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.okhttp.fingerprints.*
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.removeFinal
import app.revanced.patches.bilibili.utils.toPublic
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "OkHttp",
    description = "OkHttp网络请求响应Hook",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object OkHttpPatch : MultiMethodBytecodePatch(
    fingerprints = setOf(
        HttpUrlFingerprint,
        MediaTypeGetFingerprint,
        RequestFingerprint,
        ResponseBodyFingerprint,
        ResponseFingerprint,
    ),
    multiFingerprints = setOf(BodyWrapperFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)
        val httpUrlClass = HttpUrlFingerprint.result?.classDef
            ?: throw HttpUrlFingerprint.exception
        val requestClass = RequestFingerprint.result?.classDef
            ?: throw RequestFingerprint.exception
        val urlField = requestClass.fields.first { it.type == httpUrlClass.type }
        val responseClass = ResponseFingerprint.result?.mutableClass
            ?: throw ResponseFingerprint.exception
        responseClass.fields.forEach { it.accessFlags = it.accessFlags.removeFinal() }
        val requestField = responseClass.fields.first { it.type == requestClass.type }
        val codeField = responseClass.fields.first { it.type == "I" }
        val responseBodyClass = ResponseBodyFingerprint.result?.classDef
            ?: throw ResponseBodyFingerprint.exception
        val responseBodyField = responseClass.fields.first { it.type == responseBodyClass.type }
        val mediaTypeGetMethod = MediaTypeGetFingerprint.result?.method
            ?: throw MediaTypeGetFingerprint.exception
        val mediaTypeType = mediaTypeGetMethod.definingClass
        val createMethod = responseBodyClass.methods.first { m ->
            AccessFlags.STATIC.isSet(m.accessFlags) && m.parameterTypes.let { ts ->
                ts.size == 2 && ts[0] == mediaTypeType && ts[1] == "Ljava/lang/String;"
            }
        }
        val stringMethod = responseBodyClass.methods.first { m ->
            m.returnType == "Ljava/lang/String;" && m.parameterTypes.isEmpty()
        }
        val bodyWrapperClasses = BodyWrapperFingerprint.result.map { it.mutableClass }
            .onEach { it.accessFlags = it.accessFlags.toPublic() }
        responseClass.methods.first { it.name == "<init>" }.run {
            val insertIndex = implementation!!.instructions.indexOfLast {
                it.opcode == Opcode.RETURN_VOID
            }
            addInstructionsWithLabels(
                insertIndex, """
                iget-object p1, p0, $responseBodyField
                if-eqz p1, :jump
                ${
                    bodyWrapperClasses.joinToString(separator = "\n") {
                        """
                        instance-of v0, p1, $it
                        if-nez v0, :jump
                    """.trimIndent()
                    }
                }
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
    }
}
