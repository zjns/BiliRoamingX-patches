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
            val (register, insertIndex) = implementation!!.instructions.withIndex()
                .firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.INVOKE_VIRTUAL && inst is BuilderInstruction35c
                        && inst.reference.toString() == "Landroid/widget/CheckBox;->setChecked(Z)V"
                    ) (inst.registerD to index) else null
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
