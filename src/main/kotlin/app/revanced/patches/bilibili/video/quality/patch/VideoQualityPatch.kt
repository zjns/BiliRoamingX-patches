package app.revanced.patches.bilibili.video.quality.patch

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.exception
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerQualityServiceFingerprint
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerSettingHelperFingerprint
import com.android.tools.smali.dexlib2.iface.Method

@Patch(
    name = "Video default quality",
    description = "视频默认画质设置",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object VideoQualityPatch : MultiMethodBytecodePatch(
    fingerprints = setOf(PlayerSettingHelperFingerprint),
    multiFingerprints = setOf(PlayerQualityServiceFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)
        var defaultQnMethod: Method? = null
        PlayerSettingHelperFingerprint.result?.also {
            defaultQnMethod = it.method
        }?.mutableMethod?.addInstructionsWithLabels(
            0, """
            invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->fullScreenQuality()I
            move-result v0
            if-eqz v0, :jump
            return v0
            :jump
            nop
        """.trimIndent()
        ) ?: throw PlayerSettingHelperFingerprint.exception
        PlayerQualityServiceFingerprint.result.ifEmpty {
            throw PlayerQualityServiceFingerprint.exception
        }.forEach {
            it.mutableMethod.addInstructionsWithLabels(
                0, """
                invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->getMatchedHalfScreenQuality()I
                move-result v0
                if-eqz v0, :jump
                return v0
                :jump
                nop
            """.trimIndent()
            )
        }
        context.findClass("Lapp/revanced/bilibili/patches/VideoQualityPatch;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "defaultQn" }?.addInstructions(
                0, """
                invoke-static {}, $defaultQnMethod
                move-result v0
                return v0
            """.trimIndent()
            )
        }
    }
}
