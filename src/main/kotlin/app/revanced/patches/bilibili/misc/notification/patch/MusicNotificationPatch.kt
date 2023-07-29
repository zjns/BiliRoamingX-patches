package app.revanced.patches.bilibili.misc.notification.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.notification.fingerprints.LiveNotificationHelperFingerprint
import app.revanced.patches.bilibili.misc.notification.fingerprints.NotificationStyleAbFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("music-notification")
@Description("原生音乐通知样式")
class MusicNotificationPatch : BytecodePatch(
    fingerprints = listOf(LiveNotificationHelperFingerprint, NotificationStyleAbFingerprint)
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
        return PatchResultSuccess()
    }
}
