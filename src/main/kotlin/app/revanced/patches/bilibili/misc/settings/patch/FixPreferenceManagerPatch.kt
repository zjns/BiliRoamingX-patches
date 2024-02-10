package app.revanced.patches.bilibili.misc.settings.patch

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethodParameter.Companion.toMutable
import app.revanced.patches.bilibili.misc.settings.fingerprints.PreferenceManagerFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import app.revanced.patches.bilibili.utils.MethodParameter
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

/**
 * TODO: better way to solve it?
 */
@Patch(
    name = "Fix preference manager",
    description = "修复PreferenceManager被混淆后引起的问题",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object FixPreferenceManagerPatch : BytecodePatch(setOf(PreferenceManagerFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val preferenceManagerDef = PreferenceManagerFingerprint.result?.classDef
            ?: throw PreferenceManagerFingerprint.exception

        val checkBoxGroupPreferenceClass =
            context.findClass("Lapp/revanced/bilibili/widget/CheckBoxGroupPreference;")!!.mutableClass
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
                    it[0] = MethodParameter(preferenceManagerDef.type).toMutable()
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
                    it[0] = MethodParameter(preferenceManagerDef.type).toMutable()
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
            val (i1, r1) = implementation!!.instructions.withIndex().firstNotNullOf { (index, inst) ->
                if (inst.opcode == Opcode.CONST_CLASS) {
                    index to (inst as BuilderInstruction21c).registerA
                } else null
            }
            replaceInstruction(i1, "const-class v$r1, $preferenceManagerDef")
            val (i2, r2) = implementation!!.instructions.withIndex().firstNotNullOf { (index, inst) ->
                if (inst.opcode == Opcode.NEW_INSTANCE) {
                    index to (inst as BuilderInstruction21c).registerA
                } else null
            }
            replaceInstruction(i2, "new-instance v$r2, $preferenceManagerDef")
            val (index, registers) = implementation!!.instructions.withIndex().firstNotNullOf { (index, inst) ->
                if (inst.opcode == Opcode.INVOKE_DIRECT && inst is BuilderInstruction35c && inst.registerCount == 2) {
                    index to inst.let { arrayOf(it.registerC, it.registerD) }
                } else null
            }
            replaceInstruction(
                index, """
                invoke-direct {${registers.joinToString(separator = ",") { "v$it" }}}, $preferenceManagerDef-><init>(Landroid/content/Context;)V
            """.trimIndent()
            )
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
    }
}
