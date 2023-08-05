package app.revanced.patches.bilibili.misc.copy.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.copy.fingerprints.*
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.toErrorResult

@Patch
@BiliBiliCompatibility
@Name("copy-enhance")
@Description("自由复制补丁")
class CopyEnhancePatch : MultiMethodBytecodePatch(
    fingerprints = listOf(
        CommentCopyOldFingerprint,
        CommentCopyNewFingerprint,
        Comment3CopyFingerprint,
        ConversationCopyFingerprint
    ),
    multiFingerprints = listOf(DescCopyFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        super.execute(context)
        DescCopyFingerprint.result.ifEmpty {
            return DescCopyFingerprint.toErrorResult()
        }.forEach {
            it.mutableMethod.addInstructionsWithLabels(
                0, """
                invoke-static {p1, p2}, Lapp/revanced/bilibili/patches/CopyEnhancePatch;->onCopyDesc(ZLjava/lang/String;)Z
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
            )
        }
        val onLongClickOriginListenerType = "Lapp/revanced/bilibili/widget/OnLongClickOriginListener;"
        context.classes.filter {
            it.type.startsWith("Lcom/bilibili/bplus/followinglist/module/item")
                    && it.interfaces.contains("Landroid/view/View\$OnLongClickListener;")
        }.forEach { c ->
            context.proxy(c).mutableClass.interfaces.add(onLongClickOriginListenerType)
            context.proxy(c).mutableClass.methods.run {
                first { it.name == "onLongClick" }.also { m ->
                    m.cloneMutable(name = "onLongClick_Origin").also { add(it) }
                }.addInstructionsWithLabels(
                    0, """
                    invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/CopyEnhancePatch;->onDynamicLongClick(${onLongClickOriginListenerType}Landroid/view/View;)Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x1
                    return v0
                    :jump
                    nop
                """.trimIndent()
                )
            }
        }
        listOf(
            CommentCopyOldFingerprint.result to "message",
            CommentCopyNewFingerprint.result to "comment_message",
            Comment3CopyFingerprint.result to "comment_message"
        ).forEach { (result, idName) ->
            result?.mutableClass?.interfaces?.add(onLongClickOriginListenerType)
            result?.mutableClass?.methods?.run {
                result.mutableMethod.also { m ->
                    m.cloneMutable(name = "onLongClick_Origin").also { add(it) }
                }.addInstructionsWithLabels(
                    0, """
                    const-string v0, "$idName"
                    invoke-static {p0, p1, v0}, Lapp/revanced/bilibili/patches/CopyEnhancePatch;->onCommentLongClick(${onLongClickOriginListenerType}Landroid/view/View;Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x1
                    return v0
                    :jump
                    nop
                """.trimIndent()
                )
            }
        }
        ConversationCopyFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
            move-object/from16 v0, p8
            invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
            move-result v0
            if-eqz v0, :jump
            move-object/from16 v0, p7
            invoke-static {p0, p2, v0}, Lapp/revanced/bilibili/patches/CopyEnhancePatch;->onConversationCopy(Landroid/app/Activity;Lcom/bilibili/bplus/im/business/model/BaseTypedMessage;Landroid/widget/PopupWindow;)Z
            move-result v0
            if-eqz v0, :jump
            return-void
            :jump
            nop
        """.trimIndent()
        ) ?: return ConversationCopyFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
