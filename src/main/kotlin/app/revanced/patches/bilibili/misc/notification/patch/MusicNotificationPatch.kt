package app.revanced.patches.bilibili.misc.notification.patch

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
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.notification.fingerprints.*
import app.revanced.patches.bilibili.utils.className
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.removeFinal
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@BiliBiliCompatibility
@Name("music-notification")
@Description("原生音乐通知样式")
class MusicNotificationPatch : BytecodePatch(
    fingerprints = listOf(
        SetStateFingerprint,
        OnSeekCompleteFingerprint,
        MediaSessionCallbackOnSeekToFingerprint,
        MusicNotificationHelperFingerprint,
        LiveNotificationHelperFingerprint,
        NotificationCompatBuilderFingerprint,
        SeekToFingerprint,
        // must after SeekToFingerprint
        DefaultSpeedFingerprint,
        BackgroundPlayerFingerprint,
        PlayerServiceFingerprint,
        MusicBackgroundPlayerFingerprint,
        MusicWrapperPlayerFingerprint,
        NotificationStyleAbFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val patchClass = context.findClass("Lapp/revanced/bilibili/patches/MusicNotificationPatch;")!!.mutableClass
        patchClass.fields.forEach { it.accessFlags = it.accessFlags.removeFinal() }

        val onSetStateMethod = patchClass.methods.first { it.name == "onSetState" }
        val (absMusicServiceClass, setStateMethod) = SetStateFingerprint.result?.run {
            mutableMethod.addInstructions(
                0, """
                invoke-static {p0, p1}, $onSetStateMethod
            """.trimIndent()
            )
            mutableClass to mutableMethod
        } ?: return SetStateFingerprint.toErrorResult()

        val onUpdateMetadataMethod = patchClass.methods.first { it.name == "onUpdateMetadata" }
        absMusicServiceClass.methods.find { it.parameterTypes == listOf("Z") && it.returnType == "V" }?.addInstructions(
            0, """
                invoke-static {p0, p1}, $onUpdateMetadataMethod
                move-result p1
            """.trimIndent()
        ) ?: return PatchResultError("not found updateMetadata method")

        val onNewAbsMusicServiceMethod = patchClass.methods.first { it.name == "onNewAbsMusicService" }
        absMusicServiceClass.methods.first { it.name == "<init>" }.run {
            addInstructions(
                implementation!!.instructions.size - 1, """
                invoke-static {p0}, $onNewAbsMusicServiceMethod
            """.trimIndent()
            )
        }

        val onDestroyMusicServiceMethod = patchClass.methods.first { it.name == "onDestroyMusicService" }
        absMusicServiceClass.methods.first { it.name == "onDestroy" }.let { method ->
            method.cloneMutable(registerCount = 1, clearImplementation = true).apply {
                method.name += "_Origin"
                addInstructions(
                    """
                    invoke-virtual {p0}, $method
                    invoke-static {}, $onDestroyMusicServiceMethod
                    return-void
                """.trimIndent()
                )
            }.also { absMusicServiceClass.methods.add(it) }
        }

        val onGetFlagMethod = patchClass.methods.first { it.name == "onGetFlag" }
        absMusicServiceClass.methods.find {
            it.parameterTypes.isEmpty() && it.returnType == "J" && !AccessFlags.ABSTRACT.isSet(it.accessFlags)
        }?.let { method ->
            method.cloneMutable(registerCount = 3, clearImplementation = true).apply {
                method.name += "_Origin"
                addInstructions(
                    """
                    invoke-virtual {p0}, $method
                    move-result-wide v0
                    invoke-static/range {v0 .. v1}, $onGetFlagMethod
                    move-result-wide v0
                    return-wide v0
                """.trimIndent()
                )
            }.also { absMusicServiceClass.methods.add(it) }
        } ?: return PatchResultError("not found getFlag method")

        val onSeekCompleteMethod = patchClass.methods.first { it.name == "onSeekComplete" }
        OnSeekCompleteFingerprint.result?.mutableMethod?.addInstructions(
            0, """
            invoke-static {}, $onSeekCompleteMethod
        """.trimIndent()
        ) ?: return OnSeekCompleteFingerprint.toErrorResult()

        val onSeekToMethod = patchClass.methods.first { it.name == "onSeekTo" }
        MediaSessionCallbackOnSeekToFingerprint.result?.mutableMethod?.run {
            addInstructions(
                implementation!!.instructions.size - 1, """
                invoke-static/range {p1 .. p2}, $onSeekToMethod
            """.trimIndent()
            )
        } ?: return MediaSessionCallbackOnSeekToFingerprint.toErrorResult()

        val (playbackStateClass, setPlaybackStateMethod) = context.findSetPlaybackStateMethod()
            ?: return PatchResultError("not found setPlaybackState method")
        val onSetPlaybackStateMethod = patchClass.methods.first { it.name == "onSetPlaybackState" }
        setPlaybackStateMethod.let { method ->
            method.cloneMutable(registerCount = 14, clearImplementation = true).apply {
                method.name += "_Origin"
                addInstructions(
                    """
                    invoke-static/range {p2 .. p4}, $onSetPlaybackStateMethod
                    move-result-object v0
                    iget-object v2, v0, ${onSetPlaybackStateMethod.returnType}->first:Ljava/lang/Object;
                    check-cast v2, Ljava/lang/Number;
                    invoke-virtual {v2}, Ljava/lang/Number;->longValue()J
                    move-result-wide v2
                    iget-object v4, v0, ${onSetPlaybackStateMethod.returnType}->second:Ljava/lang/Object;
                    check-cast v4, Ljava/lang/Number;
                    invoke-virtual {v4}, Ljava/lang/Number;->floatValue()F
                    move-result v4
                    move-object v0, p0
                    move v1, p1
                    move-wide v5, p5
                    invoke-virtual/range {v0 .. v6}, $method
                    return-object p0
                """.trimIndent()
                )
            }.also { playbackStateClass.methods.add(it) }
        }

        val notificationCompatBuilderType = NotificationCompatBuilderFingerprint.result?.classDef?.type
            ?: return NotificationCompatBuilderFingerprint.toErrorResult()
        MusicNotificationHelperFingerprint.result?.mutableClass?.methods?.find { m ->
            m.parameterTypes == listOf(notificationCompatBuilderType) && m.returnType == "V"
        }?.addInstructionsWithLabels(
            0, """
            invoke-static {}, $patchClass->enabled()Z
            move-result v0
            if-eqz v0, :jump
            return-void
            :jump
            nop
        """.trimIndent()
        ) ?: return MusicNotificationHelperFingerprint.toErrorResult()

        NotificationStyleAbFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
            invoke-static {}, $patchClass->enabled()Z
            move-result v0
            if-eqz v0, :jump
            const/4 v0, 0x0
            return v0
            :jump
            nop
        """.trimIndent()
        ) ?: return NotificationStyleAbFingerprint.toErrorResult()

        val onCreateNotificationMethod = patchClass.methods.first { it.name == "onCreateNotification" }
        arrayOf(
            MusicNotificationHelperFingerprint.result?.mutableClass
                ?: return MusicNotificationHelperFingerprint.toErrorResult(),
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


        val (playerCoreServiceV2Class, seekToMethod) = SeekToFingerprint.result?.run {
            if (method.parameterTypes == listOf("I")) {
                classDef to method
            } else {
                (classDef.methods.find { m ->
                    m.parameterTypes == listOf("I") && m.returnType == "V"
                            && m.implementation?.instructions?.any { inst ->
                        inst.opcode == Opcode.INVOKE_VIRTUAL && inst is Instruction35c && inst.reference == method
                    } == true
                } ?: classDef.methods.find { m ->
                    m.parameterTypes == listOf("I", "Z") && m.returnType == "V"
                            && m.implementation?.instructions?.any { inst ->
                        inst.opcode == Opcode.INVOKE_VIRTUAL && inst is Instruction35c && inst.reference == method
                    } == true
                })?.let { classDef to it }
            }
        } ?: return SeekToFingerprint.toErrorResult()
        val defaultSpeedMethod = DefaultSpeedFingerprint.result?.method
            ?: return DefaultSpeedFingerprint.toErrorResult()

        val backgroundPlayerClass = BackgroundPlayerFingerprint.result?.classDef
            ?: return BackgroundPlayerFingerprint.toErrorResult()

        val playerServiceClassType = PlayerServiceFingerprint.result?.classDef?.superclass?.let { sc ->
            context.classes.first { it.type == sc }.interfaces.firstOrNull()
        } ?: return PlayerServiceFingerprint.toErrorResult()

        val musicBackgroundPlayerClass = MusicBackgroundPlayerFingerprint.result?.classDef
            ?: return MusicBackgroundPlayerFingerprint.toErrorResult()
        val musicWrapperPlayerClass = MusicWrapperPlayerFingerprint.result?.classDef
            ?: return MusicWrapperPlayerFingerprint.toErrorResult()
        val musicWrapperPlayerField = musicBackgroundPlayerClass.fields.find {
            it.type == musicWrapperPlayerClass.type
        } ?: return PatchResultError("not found musicWrapperPlayer field")
        val ifs = musicWrapperPlayerClass.interfaces.flatMap { i ->
            context.classes.first { it.type == i }.interfaces
        }
        val musicPlayerField = musicWrapperPlayerClass.fields.find { f ->
            ifs.contains(context.classes.first { it.type == f.type }.interfaces.firstOrNull())
        } ?: return PatchResultError("not found musicPlayer field")
        val musicPlayerServiceField = context.classes.first { it.type == musicPlayerField.type }.fields.find {
            it.type == playerServiceClassType
        } ?: return PatchResultError("not found musicPlayerService field")

        val absMusicServiceClassNameField = patchClass.fields.first { it.name == "absMusicServiceClassName" }
        val setStateMethodNameField = patchClass.fields.first { it.name == "setStateMethodName" }
        val playerCoreServiceV2ClassNameField = patchClass.fields.first { it.name == "playerCoreServiceV2ClassName" }
        val seekToMethodNameField = patchClass.fields.first { it.name == "seekToMethodName" }
        val defaultSpeedMethodNameField = patchClass.fields.first { it.name == "defaultSpeedMethodName" }
        val backgroundPlayerClassNameField = patchClass.fields.first { it.name == "backgroundPlayerClassName" }
        val playerServiceClassNameField = patchClass.fields.first { it.name == "playerServiceClassName" }
        val musicBackgroundPlayerClassNameField =
            patchClass.fields.first { it.name == "musicBackgroundPlayerClassName" }
        val musicWrapperPlayerFieldNameField = patchClass.fields.first { it.name == "musicWrapperPlayerFieldName" }
        val musicPlayerFieldNameField = patchClass.fields.first { it.name == "musicPlayerFieldName" }
        val musicPlayerServiceFieldNameField = patchClass.fields.first { it.name == "musicPlayerServiceFieldName" }
        patchClass.methods.first { it.name == "initHookInfo" }
            .also { patchClass.methods.remove(it) }
            .cloneMutable(registerCount = 1, clearImplementation = true).apply {
                addInstructions(
                    """
                    const-string v0, "${absMusicServiceClass.type.className}"
                    sput-object v0, $absMusicServiceClassNameField
                    
                    const-string v0, "${setStateMethod.name}"
                    sput-object v0, $setStateMethodNameField
                    
                    const-string v0, "${playerCoreServiceV2Class.type.className}"
                    sput-object v0, $playerCoreServiceV2ClassNameField
                    
                    const-string v0, "${seekToMethod.name}"
                    sput-object v0, $seekToMethodNameField
                    
                    const-string v0, "${defaultSpeedMethod.name}"
                    sput-object v0, $defaultSpeedMethodNameField
                    
                    const-string v0, "${backgroundPlayerClass.type.className}"
                    sput-object v0, $backgroundPlayerClassNameField
                    
                    const-string v0, "${playerServiceClassType.className}"
                    sput-object v0, $playerServiceClassNameField
                    
                    const-string v0, "${musicBackgroundPlayerClass.type.className}"
                    sput-object v0, $musicBackgroundPlayerClassNameField
                    
                    const-string v0, "${musicWrapperPlayerField.name}"
                    sput-object v0, $musicWrapperPlayerFieldNameField
                    
                    const-string v0, "${musicPlayerField.name}"
                    sput-object v0, $musicPlayerFieldNameField
                    
                    const-string v0, "${musicPlayerServiceField.name}"
                    sput-object v0, $musicPlayerServiceFieldNameField
                    
                    return-void
                """.trimIndent()
                )
            }.also { patchClass.methods.add(it) }
        return PatchResultSuccess()
    }

    private fun BytecodeContext.findSetPlaybackStateMethod(): Pair<MutableClass, MutableMethod>? {
        val playbackStatePrefix = "Landroid/support/v4/media/session/PlaybackStateCompat"
        val playbackStateClass = classes.find { it.type == "$playbackStatePrefix;" } ?: return null
        val playbackStateBuilderClass = classes.firstOrNull { c ->
            c.type.startsWith("$playbackStatePrefix\$") && c.methods.any {
                it.parameterTypes.isEmpty() && it.returnType == playbackStateClass.type
            }
        } ?: return null
        return proxy(playbackStateBuilderClass).mutableClass.run {
            methods.find { it.parameterTypes == listOf("I", "J", "F", "J") }?.let {
                this to it
            }
        }
    }
}
