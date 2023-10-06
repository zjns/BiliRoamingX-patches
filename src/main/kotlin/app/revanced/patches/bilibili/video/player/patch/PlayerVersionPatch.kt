package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.video.player.fingerprints.FFUniteDetailAbFingerprint

@Patch(
    name = "Player version",
    description = "播放器版本",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object PlayerVersionPatch : BytecodePatch(setOf(FFUniteDetailAbFingerprint)) {
    override fun execute(context: BytecodeContext) {
        fun MutableMethod.patch() = addInstructionsWithLabels(
            0, """
            invoke-static {}, Lapp/revanced/bilibili/patches/PlayerVersionPatch;->playerVersion()I
            move-result v0
            if-eqz v0, :jump
            const/4 v1, 0x2
            if-eq v0, v1, :new
            const/4 v0, 0x0
            goto :return
            :new
            const/4 v0, 0x1
            :return
            return v0
            :jump
            nop
        """.trimIndent()
        )
        // < 7.39.0
        context.findClass("Ltv/danmaku/biliplayerv2/GeminiPlayerFFKt;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.returnType == "Z" && it.parameters.isEmpty() }?.patch()
        }
        val utilsClass = context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!.mutableClass
        val newPlayerEnabledField = utilsClass.fields.first { it.name == "newPlayerEnabled" }
        // >= 7.39.0
        FFUniteDetailAbFingerprint.result?.run {
            mutableMethod.patch()
            mutableMethod.cloneMutable(registerCount = 2, clearImplementation = true).apply {
                mutableMethod.name += "_Origin"
                addInstructions(
                    """
                    invoke-direct {p0}, $mutableMethod
                    move-result v0
                    sput-boolean v0, $newPlayerEnabledField
                    return v0
                """.trimIndent()
                )
            }.also { mutableClass.methods.add(it) }
        }
    }
}
