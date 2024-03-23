package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.integrations.fingerprints.AppCompatActivityFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c

@Patch(
    name = "Dpi",
    description = "自定义dpi辅助补丁",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object DpiPatch : BytecodePatch(setOf(AppCompatActivityFingerprint)) {
    override fun execute(context: BytecodeContext) {
        AppCompatActivityFingerprint.result?.mutableClass?.methods?.run {
            first { it.name == "onConfigurationChanged" }.addInstruction(
                0, """
                invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onActivityPreConfigurationChanged(Landroid/app/Activity;Landroid/content/res/Configuration;)V
            """.trimIndent()
            )
            first { it.name == "attachBaseContext" }.addInstructions(
                0, """
                invoke-static {p0, p1}, Lapp/revanced/bilibili/patches/main/ApplicationDelegate;->onActivityPreAttachBaseContext(Landroid/app/Activity;Landroid/content/Context;)Landroid/content/Context;
                move-result-object p1
            """.trimIndent()
            )
        } ?: throw AppCompatActivityFingerprint.exception
        context.classes.forEach { c ->
            val type = c.type
            if (type != "Lapp/revanced/bilibili/patches/main/ApplicationDelegate;"
                && type != "Lapp/revanced/bilibili/patches/DpiPatch;"
            ) c.methods.filter {
                val accessFlags = it.accessFlags
                !AccessFlags.ABSTRACT.isSet(accessFlags) && !AccessFlags.NATIVE.isSet(accessFlags)
            }.forEach { m ->
                m.implementation!!.instructions.withIndex().firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.IGET && inst is Instruction22c
                        && inst.reference.toString() == "Landroid/util/DisplayMetrics;->density:F"
                    ) index to inst.registerA else null
                }?.let { (index, rA) ->
                    context.proxy(c).mutableClass.methods.first { it == m }.addInstructions(
                        index + 1, """
                        invoke-static {v$rA}, Lapp/revanced/bilibili/patches/DpiPatch;->onGetDensity(F)F
                        move-result v$rA
                    """.trimIndent()
                    )
                }
            }
        }
    }
}
