package app.revanced.patches.bilibili.misc.other.patch

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
import app.revanced.patches.bilibili.misc.other.fingerprints.FavFolderOnDataSuccessFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c

@Patch
@BiliBiliCompatibility
@Name("forbid-auto-subscribe")
@Description("禁止自动勾选订阅合集")
class FavFolderDialogPatch : BytecodePatch(listOf(FavFolderOnDataSuccessFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        FavFolderOnDataSuccessFingerprint.result?.mutableMethod?.run {
            var i = 0
            val (register, insertIndex) = implementation!!.instructions.firstNotNullOfOrNull {
                if (it.opcode == Opcode.INVOKE_VIRTUAL && it is BuilderInstruction35c
                    && it.reference.toString() == "Landroid/widget/CheckBox;->setChecked(Z)V"
                ) (it.registerD to i++) else run { i++; null }
            } ?: return FavFolderOnDataSuccessFingerprint.toErrorResult()
            addInstructions(
                insertIndex, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/FavFolderDialogPatch;->shouldChecked(Z)Z
                move-result v$register
            """.trimIndent()
            )
        } ?: return FavFolderOnDataSuccessFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
