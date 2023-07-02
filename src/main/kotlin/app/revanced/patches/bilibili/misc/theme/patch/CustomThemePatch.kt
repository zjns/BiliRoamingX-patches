package app.revanced.patches.bilibili.misc.theme.patch

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
import app.revanced.patches.bilibili.misc.theme.fingerprints.*
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.removeFinal
import app.revanced.patches.bilibili.utils.toPublic
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("custom-theme")
@Description("自定义主题色")
class CustomThemePatch : BytecodePatch(
    listOf(
        BuiltInThemesFingerprint,
        SkinListFingerprint,
        ThemeClickFingerprint,
        ThemeColorsFingerprint,
        ThemeHelperFingerprint,
        ThemeNameFingerprint,
        ThemeProcessorFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val patchClass = context.findClass("Lapp/revanced/bilibili/patches/CustomThemePatch;")!!.mutableClass

        ThemeNameFingerprint.result?.mutableClass?.fields?.first {
            it.type == "Ljava/util/Map;"
        }?.let { field ->
            field.accessFlags = field.accessFlags.toPublic().removeFinal()
            patchClass.methods.run {
                first { it.name == "getThemeNamesMap" }.also { remove(it) }.cloneMutable(
                    registerCount = 1, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        sget-object v0, $field
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: return ThemeNameFingerprint.toErrorResult()

        ThemeHelperFingerprint.result?.let { r ->
            r.mutableClass.fields.first { it.name == r.method.name }
        }?.let { field ->
            field.accessFlags = field.accessFlags.toPublic().removeFinal()
            patchClass.methods.run {
                first { it.name == "getColorArray" }.also { remove(it) }.cloneMutable(
                    registerCount = 1, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        sget-object v0, $field
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: return ThemeHelperFingerprint.toErrorResult()

        BuiltInThemesFingerprint.result?.mutableClass?.fields?.first {
            it.type == "Ljava/util/Map;"
        }?.let { field ->
            field.accessFlags = field.accessFlags.toPublic().removeFinal()
            patchClass.methods.run {
                first { it.name == "getAllThemes" }.also { remove(it) }.cloneMutable(
                    registerCount = 1, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        sget-object v0, $field
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: return BuiltInThemesFingerprint.toErrorResult()

        ThemeColorsFingerprint.result?.mutableClass?.methods?.first {
            it.name == "<init>" && AccessFlags.PRIVATE.isSet(it.accessFlags)
        }?.let { method ->
            method.accessFlags = method.accessFlags.toPublic()
            patchClass.methods.run {
                first { it.name == "newTheme" }.also { remove(it) }.cloneMutable(
                    registerCount = 36, clearImplementation = true
                ).apply {
                    addInstructions(
                        """
                        new-instance v0, ${method.definingClass}
                        move-object v1, v0
                        move-wide/from16 v4, p2
                        move-wide/from16 v6, p4
                        move-wide/from16 v8, p6
                        move-wide/from16 v10, p8
                        move-wide/from16 v12, p10
                        move-wide/from16 v14, p12
                        move-wide/from16 v16, p14
                        move/from16 v18, p16
                        move-object/from16 v2, p0
                        move-object/from16 v3, p1
                        invoke-direct/range {v1 .. v18}, $method
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            }
        } ?: return ThemeColorsFingerprint.toErrorResult()

        val onSetSkinListMethod = patchClass.methods.first { it.name == "onSetSkinList" }
        SkinListFingerprint.result?.mutableMethod?.addInstructions(
            0, """
            invoke-static {p1}, $onSetSkinListMethod
        """.trimIndent()
        ) ?: return SkinListFingerprint.toErrorResult()

        val onClickOriginListenerType = "Lapp/revanced/bilibili/widget/OnClickOriginListener;"
        val onThemeClickMethod = patchClass.methods.first { it.name == "onThemeClick" }
        ThemeClickFingerprint.result?.run {
            mutableClass.interfaces.add(onClickOriginListenerType)
            mutableMethod.also {
                mutableClass.methods.add(it.cloneMutable(name = "onClick_Origin"))
            }.addInstructionsWithLabels(
                0, """
                invoke-static {p0, p1}, $onThemeClickMethod
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
            )
        } ?: return ThemeClickFingerprint.toErrorResult()

        val onThemeResetMethod = patchClass.methods.first { it.name == "onThemeReset" }
        ThemeProcessorFingerprint.result?.mutableClass?.methods?.filter { m ->
            m.parameterTypes.isEmpty() && m.accessFlags == 0 && m.returnType == "V"
        }?.forEach { m ->
            m.addInstructionsWithLabels(
                0, """
                invoke-static {}, $onThemeResetMethod
                move-result v0
                if-eqz v0, :jump
                return-void
                :jump
                nop
            """.trimIndent()
            )
        } ?: return ThemeProcessorFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
