package app.revanced.patches.bilibili.misc.protofuf.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("make-unknown-fields-public")
@Description("使 unknownFields 字段修饰符变为 public")
class MakeUnknownFieldsPublicPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/google/protobuf/GeneratedMessageLite;")
            ?.mutableClass?.fields?.let { fields ->
                fields.find { it.name == "unknownFields" }
                    ?.accessFlags = AccessFlags.PUBLIC.value
            } ?: return PatchResultError("can not found unknownFields")
        return PatchResultSuccess()
    }
}
