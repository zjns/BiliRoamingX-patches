package app.revanced.patches.bilibili.misc.copy.patch

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
import app.revanced.patches.bilibili.misc.copy.fingerprints.CommentCopyNewFingerprint
import app.revanced.patches.bilibili.misc.copy.fingerprints.CommentCopyOldFingerprint
import app.revanced.patches.bilibili.misc.copy.fingerprints.ConversationCopyFingerprint
import app.revanced.patches.bilibili.misc.copy.fingerprints.DescCopyFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("copy-enhance")
@Description("自由复制补丁")
class CopyEnhancePatch : BytecodePatch(
    listOf(
        DescCopyFingerprint,
        CommentCopyOldFingerprint,
        CommentCopyNewFingerprint,
        ConversationCopyFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val descResult = DescCopyFingerprint.result
            ?: return DescCopyFingerprint.toErrorResult()
        val originDescMethod = descResult.method.run {
            cloneMutable(name = name + "_Origin", accessFlags = AccessFlags.PUBLIC.value)
        }.also { descResult.mutableClass.methods.add(it) }
        descResult.mutableMethod.run {
            addInstructions(
                0, """
                invoke-static {p0, p1, p2}, Lapp/revanced/bilibili/patches/CopyEnhancePatch;->copyDescProxy(Ljava/lang/Object;ZLjava/lang/String;)Z
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
            )
        }
        context.findClass("Lapp/revanced/bilibili/patches/CopyEnhancePatch;")!!
            .mutableClass.methods.run {
                first { it.name == "copyDescOrigin" }.also { remove(it) }
                    .cloneMutable(4, clearImplementation = true).apply {
                        addInstructions(
                            """
                            move-object v0, p0
                            check-cast v0, ${descResult.classDef}
                            invoke-virtual {v0, p1, p2}, $originDescMethod
                            return-void
                        """.trimIndent()
                        )
                    }.also { add(it) }
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
                }.addInstructions(
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
        ((CommentCopyOldFingerprint.result to "message") to (CommentCopyNewFingerprint.result to "comment_message"))
            .toList().forEach { (result, idName) ->
                result?.mutableClass?.interfaces?.add(onLongClickOriginListenerType)
                result?.mutableClass?.methods?.run {
                    result.mutableMethod.also { m ->
                        m.cloneMutable(name = "onLongClick_Origin").also { add(it) }
                    }.addInstructions(
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
        ConversationCopyFingerprint.result?.mutableMethod?.addInstructions(
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
