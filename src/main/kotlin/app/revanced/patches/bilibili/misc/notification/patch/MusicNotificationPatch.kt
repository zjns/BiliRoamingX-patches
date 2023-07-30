package app.revanced.patches.bilibili.misc.notification.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.notification.fingerprints.HeadsetMediaSessionCallbackFingerprint
import app.revanced.patches.bilibili.misc.notification.fingerprints.LiveNotificationHelperFingerprint
import app.revanced.patches.bilibili.misc.notification.fingerprints.MediaSessionCallbackApi21Fingerprint
import app.revanced.patches.bilibili.misc.notification.fingerprints.NotificationStyleAbFingerprint
import app.revanced.patches.bilibili.utils.*
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.AnnotationVisibility
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction22c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@BiliBiliCompatibility
@Name("music-notification")
@Description("原生音乐通知样式")
class MusicNotificationPatch : BytecodePatch(
    fingerprints = listOf(
        LiveNotificationHelperFingerprint,
        NotificationStyleAbFingerprint,
        MediaSessionCallbackApi21Fingerprint,
        HeadsetMediaSessionCallbackFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val patchClass = context.findClass("Lapp/revanced/bilibili/patches/MusicNotificationPatch;")!!.mutableClass

        NotificationStyleAbFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
            invoke-static {}, $patchClass->enabled()Z
            move-result v0
            if-eqz v0, :jump
            const/4 v0, 0x1
            return v0
            :jump
            nop
        """.trimIndent()
        ) ?: return NotificationStyleAbFingerprint.toErrorResult()

        val onCreateNotificationMethod = patchClass.methods.first { it.name == "onCreateNotification" }
        arrayOf(
            LiveNotificationHelperFingerprint.result?.mutableClass
                ?: return LiveNotificationHelperFingerprint.toErrorResult(),
        ).forEach { clazz ->
            clazz.methods.filter {
                !AccessFlags.STATIC.isSet(it.accessFlags) && it.returnType == "Landroid/app/Notification;"
            }.forEach { method ->
                val paramsSize = method.parameterTypes.size
                val registerCount = paramsSize + 2
                val op = if (AccessFlags.PRIVATE.isSet(method.accessFlags)) "invoke-direct" else "invoke-virtual"
                val registers = (0..paramsSize).joinToString(separator = ", ") { "p$it" }
                method.cloneMutable(registerCount = registerCount, clearImplementation = true).apply {
                    method.name += "_Origin"
                    addInstructions(
                        """
                        $op {$registers}, $method
                        move-result-object v0
                        invoke-static {p0, v0}, $onCreateNotificationMethod
                        move-result-object v0
                        return-object v0
                    """.trimIndent()
                    )
                }.also { clazz.methods.add(it) }
            }
        }

        val playbackStateClass = context.findClass("Landroid/support/v4/media/session/PlaybackStateCompat;")
            ?.mutableClass ?: return PatchResultError("not found PlaybackStateCompat class")
        val customActionClass =
            context.findClass("Landroid/support/v4/media/session/PlaybackStateCompat\$CustomAction;")
                ?.mutableClass ?: return PatchResultError("not found CustomAction class")
        customActionClass.methods.first {
            it.name == "<init>" && it.parameterTypes.size == 4
        }.run { accessFlags = accessFlags.toPublic() }
        val actionsFieldRef = playbackStateClass.methods.first { it.name == "<init>" }.implementation!!.instructions
            .filter { it.opcode == Opcode.IPUT_WIDE }.withIndex().firstNotNullOf { (index, inst) ->
                if (index == 2) {
                    (inst as Instruction22c).reference as FieldReference
                } else null
            }
        method(
            definingClass = playbackStateClass.type,
            name = "getActionsForBiliRoaming",
            returnType = "J",
            accessFlags = AccessFlags.PUBLIC.value,
            implementation = methodImplementation(registerCount = 3),
        ).toMutable().apply {
            addInstructions(
                """
                iget-wide v0, p0, $actionsFieldRef
                return-wide v0
            """.trimIndent()
            )
        }.also {
            playbackStateClass.methods.add(it)
        }
        val customActionsField = playbackStateClass.fields.first { it.type == "Ljava/util/List;" }
        method(
            definingClass = playbackStateClass.type,
            name = "getCustomActionsForBiliRoaming",
            returnType = "Ljava/util/List;",
            accessFlags = AccessFlags.PUBLIC.value,
            implementation = methodImplementation(registerCount = 2),
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
                                    customActionClass.type.encodedValue,
                                    ">;".encodedValue
                                )
                            )
                        )
                    )
                )
            ),
        ).toMutable().apply {
            addInstructions(
                """
                iget-object v0, p0, $customActionsField
                return-object v0
            """.trimIndent()
            )
        }.also {
            playbackStateClass.methods.add(it)
        }

        val (builderClass, buildMethod) = context.findBuildPlaybackStateMethod()
            ?: return PatchResultError("not found buildPlaybackState method")
        val onBuildPlaybackStateMethod = patchClass.methods.first { it.name == "onBuildPlaybackState" }
        buildMethod.cloneMutable(registerCount = 2, clearImplementation = true).apply {
            buildMethod.name += "_Origin"
            addInstructions(
                """
                invoke-virtual {p0}, $buildMethod
                move-result-object v0
                invoke-static {v0}, $onBuildPlaybackStateMethod
                move-result-object v0
                return-object v0
            """.trimIndent()
            )
        }.also {
            builderClass.methods.add(it)
        }

        val headsetMediaSessionCallbackClass = HeadsetMediaSessionCallbackFingerprint.result?.mutableClass
            ?: return HeadsetMediaSessionCallbackFingerprint.toErrorResult()
        val result = MediaSessionCallbackApi21Fingerprint.result
            ?: return MediaSessionCallbackApi21Fingerprint.toErrorResult()
        val mediaSessionCallbackApi21Class = result.classDef
        val onCustomActionMethodRef = result.method.implementation!!.instructions
            .last { it.opcode == Opcode.INVOKE_VIRTUAL }
            .let { (it as Instruction35c).reference as MethodReference }
        val onRewindMethodRef = mediaSessionCallbackApi21Class.methods.first { it.name == "onRewind" }
            .implementation!!.instructions.last { it.opcode == Opcode.INVOKE_VIRTUAL }
            .let { (it as Instruction35c).reference as MethodReference }
        val onFastForwardMethodRef = mediaSessionCallbackApi21Class.methods.first { it.name == "onFastForward" }
            .implementation!!.instructions.last { it.opcode == Opcode.INVOKE_VIRTUAL }
            .let { (it as Instruction35c).reference as MethodReference }
        method(
            definingClass = headsetMediaSessionCallbackClass.type,
            name = onCustomActionMethodRef.name,
            returnType = onCustomActionMethodRef.returnType,
            accessFlags = AccessFlags.PUBLIC.value,
            parameters = onCustomActionMethodRef.parameterTypes.map { methodParameter(it.toString()) },
            implementation = methodImplementation(registerCount = 4),
        ).toMutable().apply {
            addInstructionsWithLabels(
                0,
                """
                const-string v0, "Rewind"
                invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :comp_forward
                invoke-virtual {p0}, $onRewindMethodRef
                goto :return
                :comp_forward
                const-string v0, "Forward"
                invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :return
                invoke-virtual {p0}, $onFastForwardMethodRef
                :return
                return-void
            """.trimIndent()
            )
        }.also {
            headsetMediaSessionCallbackClass.methods.add(it)
        }
        return PatchResultSuccess()
    }

    private fun BytecodeContext.findBuildPlaybackStateMethod(): Pair<MutableClass, MutableMethod>? {
        val playbackStateTypePrefix = "Landroid/support/v4/media/session/PlaybackStateCompat"
        return classes.filter { it.type.startsWith("$playbackStateTypePrefix\$") }
            .firstNotNullOfOrNull { classDef ->
                classDef.methods.firstNotNullOfOrNull {
                    if (it.parameterTypes.isEmpty() && it.returnType == "$playbackStateTypePrefix;")
                        it else null
                }?.let { m ->
                    proxy(classDef).mutableClass.let {
                        it to it.findMutableMethodOf(m)
                    }
                }
            }
    }
}
