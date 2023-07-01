package app.revanced.patches.bilibili.video.player.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.video.player.fingerprints.PlayerSettingServiceFingerprint

@Patch
@BiliBiliCompatibility
@Name("default-playback-speed")
@Description("自定义播放器默认播放速度")
class DefaultPlaybackSpeedPatch : BytecodePatch(listOf(PlayerSettingServiceFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PlayerSettingServiceFingerprint.result?.mutableClass?.methods?.find { m ->
            m.returnType == "F" && m.parameterTypes.let {
                it.size == 2 && it[0] == "Ljava/lang/String;" && it[1] == "F"
            }
        }?.addInstructionsWithLabels(
            0, """
            const-string v0, "player_key_video_speed"
            invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
            move-result v0
            if-eqz v0, :jump
            invoke-static {p2}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->defaultSpeed(F)F
            move-result p2
            :jump
            nop
        """.trimIndent()
        ) ?: return PlayerSettingServiceFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
