package app.revanced.patches.bilibili.misc.protobuf.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("comment-word-search-url")
@Description("屏蔽评论关键词搜索功能")
@Version("0.0.1")
class CommentReplyUrlPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bapis/bilibili/main/community/reply/v1/Content;")
            ?.mutableClass?.methods?.find { it.name == "internalGetUrls" }?.addInstruction(
                1, """
                invoke-static {v0}, Lapp/revanced/bilibili/patches/CommentReplyUrlPatch;->filterUrls(Lcom/google/protobuf/MapFieldLite;)V
            """.trimIndent()
            ) ?: return PatchResultError("can not found internalGetUrls method")
        return PatchResultSuccess()
    }
}
