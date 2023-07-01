package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.toErrorResult
import app.revanced.patches.bilibili.video.player.fingerprints.TripleSpeedServiceFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("long-press-playback-speed")
@Description("自定义播放器长按播放速度")
class LongPressPlaybackSpeedPatch : MultiMethodBytecodePatch(
    multiFingerprints = listOf(TripleSpeedServiceFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        super.execute(context)
        TripleSpeedServiceFingerprint.result.mapNotNull { r ->
            r.method.implementation!!.instructions.firstNotNullOfOrNull { inst ->
                if (inst is Instruction35c && inst.opcode == Opcode.INVOKE_DIRECT) {
                    (inst.reference as MethodReference).takeIf {
                        it.returnType == "V" && it.parameterTypes == listOf("F")
                    }
                } else null
            }?.let { r.mutableClass.methods.first { m -> m == it } }
        }.ifEmpty {
            return TripleSpeedServiceFingerprint.toErrorResult()
        }.forEach {
            it.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->longPressSpeed(F)F
                move-result p1 
            """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
