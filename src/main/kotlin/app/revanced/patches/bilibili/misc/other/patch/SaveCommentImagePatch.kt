package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("save-comment-image")
@Description("保存评论图片补丁")
class SaveCommentImagePatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bilibili/lib/imageviewer/fragment/ImageFragment;")
            ?.mutableClass?.methods?.find { it.name == "onViewCreated" }?.run {
                val insertIndex = implementation!!.instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
                addInstruction(
                    insertIndex, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/CommentImagePatch;->bindClickListener(Lcom/bilibili/lib/imageviewer/fragment/ImageFragment;)V
                """.trimIndent()
                )
            } ?: return PatchResultError("can not found ImageFragment#onViewCreated method")
        return PatchResultSuccess()
    }
}
