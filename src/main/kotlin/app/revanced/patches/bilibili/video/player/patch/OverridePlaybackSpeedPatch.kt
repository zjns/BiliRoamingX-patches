package app.revanced.patches.bilibili.video.player.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.*
import app.revanced.patches.bilibili.video.player.fingerprints.*
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.AnnotationVisibility
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21ih
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@BiliBiliCompatibility
@Name("override-playback-speed")
@Description("自定义播放器播放速度列表")
class OverridePlaybackSpeedPatch : MultiMethodBytecodePatch(
    fingerprints = listOf(
        StoryMenuFingerprint,
        MenuFuncSegmentFingerprint,
        NewShareServiceFingerprint,
        MusicPlayerPanelFingerprint
    ),
    multiFingerprints = listOf(
        SpeedFunctionWidgetFingerprint,
        PlaybackSpeedSettingFingerprint,
        PlayerSpeedWidgetFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        super.execute(context)
        SpeedFunctionWidgetFingerprint.result.mapNotNull { r ->
            r.classDef.fields.firstNotNullOfOrNull { f ->
                context.classes.find { it.type == f.type }?.takeIf {
                    it.interfaces == listOf("Landroid/view/View\$OnClickListener;")
                }?.let { c ->
                    context.proxy(c).mutableClass.methods.first { it.name == "<init>" }
                }
            }
        }.ifEmpty {
            return SpeedFunctionWidgetFingerprint.toErrorResult()
        }.forEach { m ->
            val result = m.implementation!!.instructions.withIndex().firstNotNullOfOrNull { (index, inst) ->
                if (inst.opcode == Opcode.IPUT_OBJECT && inst is BuilderInstruction22c && (inst.reference as FieldReference).type == "[F") {
                    index to inst.registerA
                } else null
            }
            if (result != null) {
                val (insertIndex, register) = result
                m.addInstructions(
                    insertIndex, """
                    invoke-static {v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->getOverrideSpeedArray([F)[F
                    move-result-object v$register
                """.trimIndent()
                )
            }
        }
        StoryMenuFingerprint.result?.mutableClass?.methods?.first { it.name == "<init>" }?.run {
            val (register, insertIndex) = implementation!!.instructions.withIndex()
                .firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.IPUT_OBJECT && inst is BuilderInstruction22c
                        && (inst.reference as FieldReference).type == "[F"
                    ) (inst.registerA to index) else null
                } ?: return StoryMenuFingerprint.toErrorResult()
            addInstructions(
                insertIndex, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->getOverrideSpeedArray([F)[F
                move-result-object v$register
            """.trimIndent()
            )
        } ?: return StoryMenuFingerprint.toErrorResult()
        MenuFuncSegmentFingerprint.result?.mutableClass?.fields
            ?.find { it.type == "[F" && AccessFlags.STATIC.isSet(it.accessFlags) }?.let { f ->
                f.accessFlags = f.accessFlags.toPublic().removeFinal()
                context.findClass("Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;")!!.mutableClass.methods
                    .first { it.name == "refreshMenuFuncSegmentSpeedArray" }.addInstructions(
                        0, """
                        sput-object p0, $f
                    """.trimIndent()
                    )
            } ?: MenuFuncSegmentFingerprint.result?.mutableClass?.methods?.first { it.name == "<init>" }?.run {
            val (register, insertIndex) = implementation!!.instructions.withIndex()
                .firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.IPUT_OBJECT && inst is BuilderInstruction22c
                        && (inst.reference as FieldReference).type == "[F"
                    ) (inst.registerA to index) else null
                } ?: return MenuFuncSegmentFingerprint.toErrorResult()
            addInstructions(
                insertIndex, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->getOverrideSpeedArray([F)[F
                move-result-object v$register
            """.trimIndent()
            )
        } ?: return MenuFuncSegmentFingerprint.toErrorResult()
        NewShareServiceFingerprint.result?.mutableClass?.fields?.find { it.type == "[F" }?.let { f ->
            f.accessFlags = f.accessFlags.toPublic().removeFinal()
            context.findClass("Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;")!!.mutableClass.methods
                .first { it.name == "refreshNewShareServiceSpeedArray" }.addInstructions(
                    0, """
                    sput-object p0, $f
                """.trimIndent()
                )
        } ?: return NewShareServiceFingerprint.toErrorResult()
        PlaybackSpeedSettingFingerprint.result.map { r ->
            r.mutableClass.methods.first { it.name == "<init>" }
        }.ifEmpty {
            return PlaybackSpeedSettingFingerprint.toErrorResult()
        }.forEach { m ->
            val insertIndex = m.implementation!!.instructions.indexOfLast {
                it.opcode == Opcode.IPUT_OBJECT && it is BuilderInstruction22c
                        && (it.reference as FieldReference).type == "Landroid/widget/TextView;"
            }
            m.addInstructions(
                insertIndex, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onNewPlaybackSpeedSetting(Ljava/lang/Object;)V
                """.trimIndent()
            )
        }
        context.findClass("Lcom/bilibili/music/podcast/view/PodcastSpeedSeekBar;")?.mutableClass?.run {
            val speedNameListField = fields.first { it.type == "Ljava/util/List;" }
            val speedArrayField = fields.first { it.type == "[F" }.apply {
                accessFlags = accessFlags.removeFinal()
            }
            methods.find { it.name == "<init>" && it.parameterTypes.size == 3 }?.run {
                val insertIndex = implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.RETURN_VOID
                }
                addInstructions(
                    insertIndex, """
                    invoke-static {p0}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onNewPodcastSpeedSeekBar(Lcom/bilibili/music/podcast/view/PodcastSpeedSeekBar;)V
                """.trimIndent()
                )
            }
            method(
                definingClass = type,
                name = "getSpeedNameListForBiliRoaming",
                returnType = "Ljava/util/List;",
                accessFlags = AccessFlags.PUBLIC.value,
                annotations = setOf(
                    annotation(
                        visibility = AnnotationVisibility.SYSTEM,
                        type = "Ldalvik/annotation/Signature;",
                        elements = setOf(
                            annotationElement(
                                name = "value",
                                value = arrayEncodedValue(
                                    value = listOf(
                                        "()".encodedValue,
                                        "Ljava/util/List<".encodedValue,
                                        "Lkotlin/Pair<".encodedValue,
                                        "Ljava/lang/Float;".encodedValue,
                                        "Ljava/lang/String;".encodedValue,
                                        ">;>;".encodedValue
                                    )
                                )
                            )
                        )
                    )
                ),
                implementation = methodImplementation(2)
            ).toMutable().also { methods.add(it) }.addInstructions(
                """
                iget-object v0, p0, $speedNameListField
                return-object v0
            """.trimIndent()
            )
            method(
                definingClass = type,
                name = "setSpeedArrayForBiliRoaming",
                "V",
                accessFlags = AccessFlags.PUBLIC.value,
                parameters = listOf(methodParameter(type = "[F", name = "array")),
                implementation = methodImplementation(registerCount = 2)
            ).toMutable().also { methods.add(it) }.addInstructions(
                """
                iput-object p1, p0, $speedArrayField
                return-void
            """.trimIndent()
            )
        } /*?: return PatchResultError("not found PodcastSpeedSeekBar")*/ // not exist on hd
        MusicPlayerPanelFingerprint.result?.mutableClass?.methods?.first { it.name == "<init>" }?.run {
            val (register, insertIndex) = implementation!!.instructions.withIndex()
                .firstNotNullOfOrNull { (index, inst) ->
                    if (inst.opcode == Opcode.IPUT_OBJECT && inst is BuilderInstruction22c
                        && (inst.reference as FieldReference).type == "[F"
                    ) (inst.registerA to index) else null
                } ?: return MusicPlayerPanelFingerprint.toErrorResult()
            addInstructions(
                insertIndex, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->getOverrideReverseSpeedArray([F)[F
                move-result-object v$register
            """.trimIndent()
            )
        } /*?: return MusicPlayerPanelFingerprint.toErrorResult()*/ // not exist on hd
        PlayerSpeedWidgetFingerprint.result.mapNotNull { r ->
            r.mutableClass.methods.firstNotNullOfOrNull { m ->
                m.implementation?.instructions?.indexOfFirst {
                    it.opcode == Opcode.CONST && it is BuilderInstruction31i && it.wideLiteral == 0x3ffeb852L // 1.99f
                }?.takeIf { it != -1 }?.let { insertIndex ->
                    val oneIndex = m.implementation!!.instructions.indexOfFirst {
                        it.opcode == Opcode.CONST_HIGH16 && it is BuilderInstruction21ih && it.wideLiteral == 0x3f800000L // 1.0f
                    }
                    Triple(m, insertIndex, oneIndex)
                }
            }
        }.ifEmpty {
            return PlayerSpeedWidgetFingerprint.toErrorResult()
        }.forEach { (m, insertIndex, oneIndex) ->
            m.addInstructionsWithLabels(
                insertIndex,
                """
                goto :cmp_one
            """.trimIndent(),
                ExternalLabel("cmp_one", m.getInstruction(oneIndex))
            )
        }
        return PatchResultSuccess()
    }
}
