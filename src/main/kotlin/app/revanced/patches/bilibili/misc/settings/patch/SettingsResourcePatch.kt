package app.revanced.patches.bilibili.misc.settings.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.DomFileEditor
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeArrays
import app.revanced.util.resources.ResourceUtils.mergeStrings

@Patch
@Name("settings-resource-patch")
@SettingsCompatibility
@DependsOn([ResourceMappingPatch::class])
@Version("0.0.1")
@Description("哔哩漫游设置入口")
class SettingsResourcePatch : AbstractSettingsResourcePatch(
    "biliroaming_settings", "bilibili"
) {
    private val extraPreferences = arrayOf(
        "biliroaming_setting_half_screen_quality",
        "biliroaming_setting_full_screen_quality",
        "biliroaming_setting_live_popups",
        "biliroaming_setting_customize_mine",
        "biliroaming_setting_customize_drawer",
    )

    override fun execute(context: ResourceContext): PatchResult {
        super.execute(context)
        extraPreferences.forEach { prefs ->
            ResourceUtils.ResourceGroup("xml", "$prefs.xml").let {
                context.copyResources("bilibili", it)
            }
        }
        context.mergeStrings("bilibili/host/values/strings.xml")
        context.mergeArrays("bilibili/host/values/arrays.xml")
        context.xmlEditor["res/xml/main_preferences.xml"].use {
            it.addBiliRoamingEntrance()
        }
        return PatchResultSuccess()
    }

    private fun DomFileEditor.addBiliRoamingEntrance() {
        file.getElementsByTagName("androidx.preference.PreferenceScreen").item(0).run {
            ownerDocument.createElement("androidx.preference.PreferenceCategory").apply {
                ownerDocument.createElement("androidx.preference.PreferenceScreen").apply {
                    setAttribute("android:title", "@string/biliroaming_settings_title")
                    setAttribute(
                        "android:fragment",
                        "app.revanced.bilibili.settings.fragments.BiliRoamingSettingsFragment"
                    )
                }.also { appendChild(it) }
            }.also { insertBefore(it, firstChild) }
        }
    }
}
