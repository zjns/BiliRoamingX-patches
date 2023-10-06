package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.FavFolderOnDataSuccessFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

@Patch(
    name = "Forbid auto subscribe",
    description = "禁止自动勾选订阅合集",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object FavFolderDialogPatch : BytecodePatch(setOf(FavFolderOnDataSuccessFingerprint)) {
    override fun execute(context: BytecodeContext) {
        FavFolderOnDataSuccessFingerprint.result?.mutableMethod?.run {
            val (register, insertIndex) = implementation!!.instructions.withIndex()
                .firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.INVOKE_VIRTUAL && inst is BuilderInstruction35c
                        && inst.reference.toString() == "Landroid/widget/CheckBox;->setChecked(Z)V"
                    ) (inst.registerD to index) else null
                } ?: throw FavFolderOnDataSuccessFingerprint.exception
            addInstructions(
                insertIndex, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/FavFolderDialogPatch;->shouldChecked(Z)Z
                move-result v$register
            """.trimIndent()
            )
        } ?: throw FavFolderOnDataSuccessFingerprint.exception
    }
}
