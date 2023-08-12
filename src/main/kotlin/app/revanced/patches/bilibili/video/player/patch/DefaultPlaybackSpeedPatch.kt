package app.revanced.patches.bilibili.video.player.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.video.player.fingerprints.PlayerOnPreparedFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("default-playback-speed")
@Description("自定义播放器默认播放速度")
class DefaultPlaybackSpeedPatch : BytecodePatch(listOf(PlayerOnPreparedFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PlayerOnPreparedFingerprint.result?.mutableMethod?.run {
            val (index, register) = implementation!!.instructions.withIndex().firstNotNullOfOrNull { (index, inst) ->
                if (inst.opcode == Opcode.INVOKE_VIRTUAL && ((inst as BuilderInstruction35c).reference as MethodReference)
                        .let { it.parameterTypes == listOf("F") && it.returnType == "V" }
                ) index to inst.registerD else null
            } ?: return PatchResultError("not found updateSpeed method")
            addInstructions(
                index, """
                invoke-static {p1, v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->defaultSpeed(Ltv/danmaku/ijk/media/player/IMediaPlayer;F)F
                move-result v$register
            """.trimIndent()
            )
        } ?: return PlayerOnPreparedFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
