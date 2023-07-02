package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.BLKVFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("blkv")
@Description("集成BLKV")
class BLKVPatch : BytecodePatch(listOf(BLKVFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val utilsClass = context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!
        val result = BLKVFingerprint.result ?: return BLKVFingerprint.toErrorResult()
        val stockByNameMethod = result.method
        val stockByFileMethod = result.classDef.methods.first { m ->
            !AccessFlags.SYNTHETIC.isSet(m.accessFlags) && AccessFlags.STATIC.isSet(m.accessFlags)
                    && m.parameterTypes == listOf("Landroid/content/Context;", "Ljava/io/File;", "Z", "I")
        }
        val utilsGetContextMethod = utilsClass.mutableClass.methods.first { it.name == "getContext" }
        utilsClass.mutableClass.methods.run {
            first { it.name == "blkvPrefsByName" }.also { remove(it) }.cloneMutable(
                registerCount = 4, clearImplementation = true
            ).apply {
                addInstructions(
                    """
                    invoke-static {}, $utilsGetContextMethod
                    move-result-object v0
                    const/4 v1, 0x0
                    invoke-static {v0, p0, p1, v1}, $stockByNameMethod
                    move-result-object v0
                    return-object v0
                """.trimIndent()
                )
            }.also { add(it) }
            first { it.name == "blkvPrefsByFile" }.also { remove(it) }.cloneMutable(
                registerCount = 4, clearImplementation = true
            ).apply {
                addInstructions(
                    """
                    invoke-static {}, $utilsGetContextMethod
                    move-result-object v0
                    const/4 v1, 0x0
                    invoke-static {v0, p0, p1, v1}, $stockByFileMethod
                    move-result-object v0
                    return-object v0
                """.trimIndent()
                )
            }.also { add(it) }
        }
        return PatchResultSuccess()
    }
}
