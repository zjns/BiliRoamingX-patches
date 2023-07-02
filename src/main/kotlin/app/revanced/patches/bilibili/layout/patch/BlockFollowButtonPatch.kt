package app.revanced.patches.bilibili.layout.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility

@Patch
@BiliBiliCompatibility
@Name("block-follow-button")
@Description("不显示关注按钮")
class BlockFollowButtonPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Lcom/bapis/bilibili/main/community/reply/v1/ReplyControl;")
            ?.mutableClass?.methods?.find { it.name == "getShowFollowBtn" }?.run {
                addInstructions(
                    implementation!!.instructions.size - 1, """
                        invoke-static {v0}, Lapp/revanced/bilibili/patches/BlockFollowButtonPatch;->shouldShowCommentFollow(Z)Z
                        move-result v0
                    """.trimIndent()
                )
            }
        arrayOf(
            "Lcom/bapis/bilibili/app/dynamic/v2/ModuleAuthor;",
            "Lcom/bapis/bilibili/app/dynamic/v2/ModuleAuthorForward;"
        ).forEach { name ->
            context.findClass(name)?.mutableClass?.methods?.find { it.name == "getShowFollow" }?.run {
                addInstructions(
                    implementation!!.instructions.size - 1, """
                        invoke-static {v0}, Lapp/revanced/bilibili/patches/BlockFollowButtonPatch;->shouldShowDynamicFollow(Z)Z
                        move-result v0
                    """.trimIndent()
                )
            }
        }
        return PatchResultSuccess()
    }
}
