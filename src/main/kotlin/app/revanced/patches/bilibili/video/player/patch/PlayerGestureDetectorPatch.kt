package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.utils.isInterface

@Patch(
    name = "Player gesture detector hook",
    description = "播放器 GestureDetector hook",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object PlayerGestureDetectorPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        context.classes.firstNotNullOfOrNull { cl ->
            if (cl.fields.count() == 3 && cl.fields.any {
                    it.type == "Landroid/view/GestureDetector;"
                } && cl.fields.any { f ->
                    context.classes.find { it.type == f.type }?.let { c ->
                        c.accessFlags.isInterface() && c.methods.singleOrNull()?.let {
                            it.returnType == "V" && it.parameterTypes == listOf("Landroid/view/MotionEvent;")
                        } == true
                    } == true
                }) {
                cl.fields.firstNotNullOfOrNull { f ->
                    context.classes.find { it.type == f.type }?.takeIf {
                        it.superclass == "Landroid/view/GestureDetector\$SimpleOnGestureListener;"
                    }
                }?.let { context.proxy(it).mutableClass }
            } else null
        }?.methods?.firstOrNull {
            it.name == "onLongPress" && it.parameterTypes == listOf("Landroid/view/MotionEvent;")
        }?.addInstructionsWithLabels(
            0, """
            invoke-static {}, Lapp/revanced/bilibili/patches/PlayerGestureDetectorPatch;->disableLongPress()Z
            move-result v0
            if-eqz v0, :jump
            return-void
            :jump
            nop
        """.trimIndent()
        ) ?: throw PatchException("not found PlayerGestureDetector class")
    }
}
