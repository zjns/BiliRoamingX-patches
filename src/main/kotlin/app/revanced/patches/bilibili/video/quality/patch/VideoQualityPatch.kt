package app.revanced.patches.bilibili.video.quality.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.video.quality.annotations.VideoQualityCompatibility
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerPreloadHolderFingerprint
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerQualityServiceFingerprint
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerSettingHelperFingerprint

@Patch
@Name("video-default-quality")
@VideoQualityCompatibility
@Description("视频默认画质设置")
@Version("0.0.1")
class VideoQualityPatch : BytecodePatch(
    listOf(
        PlayerSettingHelperFingerprint,
        PlayerPreloadHolderFingerprint,
        PlayerQualityServiceFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        var playerSettingHelperClassType = ""
        var defaultQnMethodName = ""
        PlayerSettingHelperFingerprint.result?.also {
            playerSettingHelperClassType = it.classDef.type
            defaultQnMethodName = it.method.name
        }?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->fullScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    return v0
                    :jump
                    nop
                """.trimIndent()
        ) ?: return PlayerSettingHelperFingerprint.toErrorResult()
        PlayerPreloadHolderFingerprint.result?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->halfScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x0
                    return-object v0
                    :jump
                    nop
            """.trimIndent()
        ) ?: return PlayerPreloadHolderFingerprint.toErrorResult()
        PlayerQualityServiceFingerprint.result?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->getMatchedHalfScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    return v0
                    :jump
                    nop
        """.trimIndent()
        ) ?: return PlayerQualityServiceFingerprint.toErrorResult()
        context.findClass("Lcom/bapis/bilibili/pgc/gateway/player/v1/PlayURLMoss;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "playView" && it.parameters.size == 1 }?.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->unlockLimit(Lcom/bapis/bilibili/pgc/gateway/player/v1/PlayViewReq;)V
                """.trimIndent()
            )
        }
        context.findClass("Lcom/bapis/bilibili/pgc/gateway/player/v2/PlayURLMoss;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "playView" && it.parameters.size == 1 }?.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->unlockLimit(Lcom/bapis/bilibili/pgc/gateway/player/v2/PlayViewReq;)V
                """.trimIndent()
            )
        }
        context.findClass("Lcom/bapis/bilibili/app/playurl/v1/PlayURLMoss;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "playView" && it.parameters.size == 1 }?.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->unlockLimit(Lcom/bapis/bilibili/app/playurl/v1/PlayViewReq;)V
                """.trimIndent()
            )
        }
        context.findClass("Lcom/bapis/bilibili/app/playerunite/v1/PlayerMoss;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "playViewUnite" && it.parameters.size == 1 }?.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->unlockLimit(Lcom/bapis/bilibili/app/playerunite/v1/PlayViewUniteReq;)V
                """.trimIndent()
            )
        }
        context.findClass("Lapp/revanced/bilibili/patches/VideoQualityPatch;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "defaultQn" }?.addInstructions(
                0, """
                invoke-static {}, $playerSettingHelperClassType->$defaultQnMethodName()I
                move-result v0
                return v0
                """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
