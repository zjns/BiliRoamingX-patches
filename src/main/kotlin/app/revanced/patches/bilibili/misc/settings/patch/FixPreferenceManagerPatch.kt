package app.revanced.patches.bilibili.misc.settings.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethodParameter.Companion.toMutable
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.settings.fingerprints.PreferenceManagerFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.methodParameter
import org.jf.dexlib2.Opcode

/**
 * TODO: better way to solve it?
 */
@Patch
@BiliBiliCompatibility
@Name("fix-preference-manager")
@Description("修复PreferenceManager被混淆后引起的问题")
class FixPreferenceManagerPatch : BytecodePatch(listOf(PreferenceManagerFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val preferenceManagerDef = PreferenceManagerFingerprint.result?.classDef
            ?: return PreferenceManagerFingerprint.toErrorResult()

        val checkBoxGroupPreferenceClass =
            context.findClass("Lapp/revanced/bilibili/widget/CheckBoxGroupPreference;")!!.mutableClass
        checkBoxGroupPreferenceClass.methods.first { it.name == "notifyChanged" }.replaceInstruction(
            0, """
            invoke-virtual {p0}, Landroidx/preference/PreferenceCategory;->getPreferenceManager()$preferenceManagerDef
        """.trimIndent()
        )
        checkBoxGroupPreferenceClass.methods.run {
            first { it.name == "onAttachedToHierarchy" }.apply {
                replaceInstruction(
                    1, """
                    invoke-super {p0, p1}, Landroidx/preference/PreferenceCategory;->onAttachedToHierarchy($preferenceManagerDef)V
                """.trimIndent()
                )
            }.run {
                remove(this)
                cloneMutable(parameters = parameters.also {
                    it[0] = methodParameter(preferenceManagerDef.type).toMutable()
                }).let { add(it) }
            }
            first { it.name == "notifyChanged" }.run {
                replaceInstruction(
                    0, """
                    invoke-virtual {p0}, Landroidx/preference/PreferenceCategory;->getPreferenceManager()$preferenceManagerDef
                """.trimIndent()
                )
                replaceInstruction(
                    2, """
                    invoke-virtual {p0, v0}, Lapp/revanced/bilibili/widget/CheckBoxGroupPreference;->onAttachedToHierarchy($preferenceManagerDef)V
                """.trimIndent()
                )
            }
        }

        val ktUtilsClass = context.findClass("Lapp/revanced/bilibili/utils/KtUtils;")!!.mutableClass
        ktUtilsClass.methods.first { it.name == "retrieveOnPreferenceTreeClickListenerField" }.replaceInstruction(
            0, """
            const-class v0, $preferenceManagerDef
        """.trimIndent()
        )
        val onPreferenceTreeClickMethod = ktUtilsClass.methods.run {
            first { it.name == "onPreferenceTreeClick" }.run {
                remove(this)
                cloneMutable(parameters = parameters.also {
                    it[0] = methodParameter(preferenceManagerDef.type).toMutable()
                }).also { add(it) }
            }
        }

        val settingsFragmentClass =
            context.findClass("Lapp/revanced/bilibili/settings/fragments/BiliRoamingBaseSettingFragment;")!!.mutableClass
        val getSharedPreferencesMethod = preferenceManagerDef.methods.first {
            it.parameters.isEmpty() && it.returnType == "Landroid/content/SharedPreferences;"
        }
        val setSharedPreferencesNameMethod = preferenceManagerDef.methods.first {
            it.parameterTypes == listOf("Ljava/lang/String;") && it.returnType == "V"
        }
        settingsFragmentClass.methods.first { it.name == "fixPreferenceManager" }.run {
            implementation!!.instructions.indexOfFirst { it.opcode == Opcode.CONST_CLASS }.run {
                replaceInstruction(
                    this, """
                    const-class v1, $preferenceManagerDef
                """.trimIndent()
                )
            }
            implementation!!.instructions.indexOfFirst { it.opcode == Opcode.NEW_INSTANCE }.run {
                replaceInstruction(
                    this, """
                    new-instance v1, $preferenceManagerDef
                """.trimIndent()
                )
            }
            implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_DIRECT }.run {
                replaceInstruction(
                    this, """
                    invoke-direct {v1, v4}, $preferenceManagerDef-><init>(Landroid/content/Context;)V
                """.trimIndent()
                )
            }
        }
        settingsFragmentClass.methods.first { it.name == "onCreate" }.run {
            replaceInstruction(
                1, """
                invoke-virtual {p0}, Lcom/bilibili/lib/ui/BasePreferenceFragment;->getPreferenceManager()$preferenceManagerDef
            """.trimIndent()
            )
            replaceInstruction(
                3, """
                invoke-virtual {p1}, $getSharedPreferencesMethod
            """.trimIndent()
            )
        }
        settingsFragmentClass.methods.first { it.name == "onCreatePreferences" }.run {
            replaceInstruction(
                1, """
                invoke-virtual {p0}, Lcom/bilibili/lib/ui/BasePreferenceFragment;->getPreferenceManager()$preferenceManagerDef
            """.trimIndent()
            )
            replaceInstruction(
                4, """
                invoke-virtual {p1, p2}, $setSharedPreferencesNameMethod
            """.trimIndent()
            )
        }
        settingsFragmentClass.methods.first { it.name == "onDestroy" }.run {
            replaceInstruction(
                0, """
                invoke-virtual {p0}, Lcom/bilibili/lib/ui/BasePreferenceFragment;->getPreferenceManager()$preferenceManagerDef
            """.trimIndent()
            )
            replaceInstruction(
                2, """
                invoke-virtual {v0}, $getSharedPreferencesMethod
            """.trimIndent()
            )
        }
        settingsFragmentClass.methods.first { it.name == "onStart" }.run {
            implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_VIRTUAL }.run {
                replaceInstruction(
                    this, """
                    invoke-virtual {p0}, Lcom/bilibili/lib/ui/BasePreferenceFragment;->getPreferenceManager()$preferenceManagerDef
                """.trimIndent()
                )
            }
            implementation!!.instructions.indexOfLast { it.opcode == Opcode.INVOKE_STATIC }.run {
                replaceInstruction(
                    this, """
                    invoke-static {v0, p0}, $onPreferenceTreeClickMethod
                """.trimIndent()
                )
            }
        }
        settingsFragmentClass.methods.first { it.name == "onStop" }.run {
            implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_VIRTUAL }.run {
                replaceInstruction(
                    this, """
                    invoke-virtual {p0}, Lcom/bilibili/lib/ui/BasePreferenceFragment;->getPreferenceManager()$preferenceManagerDef
                """.trimIndent()
                )
            }
            implementation!!.instructions.indexOfLast { it.opcode == Opcode.INVOKE_STATIC }.run {
                replaceInstruction(
                    this, """
                    invoke-static {v0, p0}, $onPreferenceTreeClickMethod
                """.trimIndent()
                )
            }
        }
        return PatchResultSuccess()
    }
}
