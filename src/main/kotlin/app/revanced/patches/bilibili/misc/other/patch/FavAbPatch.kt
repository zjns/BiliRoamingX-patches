package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.FavSnackBarUtilFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

@Patch(
    name = "Fav ab",
    description = "收藏 AB 实验",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object FavAbPatch : BytecodePatch(setOf(FavSnackBarUtilFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val classType = FavSnackBarUtilFingerprint.result?.classDef?.type
            ?: throw FavSnackBarUtilFingerprint.exception
        val utilType = classType.substringBefore('$') + ";"
        context.findClass { it.type == utilType }?.mutableClass?.methods?.find {
            it.returnType == "Z" && it.parameterTypes.isEmpty() && AccessFlags.STATIC.isSet(it.accessFlags)
        }?.addInstructionsWithLabels(
            0, """
                invoke-static {}, Lapp/revanced/bilibili/patches/FavAbPatch;->oldFav()Z
                move-result v0
                if-eqz v0, :jump
                const/4 v0, 0x0
                return v0
                :jump
                nop
            """.trimIndent()
        ) ?: throw PatchException("not found FavSnackBarUtilKt class")
    }
}
