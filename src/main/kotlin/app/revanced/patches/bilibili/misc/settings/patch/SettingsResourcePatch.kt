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
        "biliroaming_setting_half_screen_quality.xml",
        "biliroaming_setting_full_screen_quality.xml",
        "biliroaming_setting_live_popups.xml",
        "biliroaming_setting_customize_mine.xml",
        "biliroaming_setting_customize_drawer.xml",
        "biliroaming_setting_customize_bottom.xml",
        "biliroaming_setting_customize_home.xml",
        "biliroaming_setting_home_tab.xml",
        "biliroaming_setting_filter_home.xml",
        "biliroaming_setting_filter_home_by_type.xml",
        "biliroaming_setting_customize_dynamic.xml",
        "biliroaming_setting_filter_dynamic_by_type.xml",
        "biliroaming_setting_customize_live_room.xml",
        "biliroaming_setting_customize_player.xml",
        "biliroaming_setting_customize_video_detail.xml",
        "biliroaming_setting_customize_space.xml",
        "biliroaming_setting_customize_subtitle.xml",
        "biliroaming_setting_unlock_area_limit.xml",
        "biliroaming_setting_upos.xml",
        "biliroaming_setting_space.xml",
        "biliroaming_setting_customize_search.xml",
        "biliroaming_setting_filter_search_type.xml",
        "biliroaming_setting_about.xml",
        "biliroaming_setting_video_detail_filter.xml",
        "biliroaming_setting_block_follow.xml",
        "biliroaming_setting_filter_story.xml",
        "biliroaming_setting_misc.xml",
    )
    private val layouts = arrayOf(
        "biliroaming_dialog_argb_color_choose.xml",
        "biliroaming_dialog_customize_backup.xml",
        "biliroaming_dialog_color_choose.xml",
    )

    override fun execute(context: ResourceContext): PatchResult {
        super.execute(context)
        arrayOf(
            ResourceUtils.ResourceGroup("xml", *extraPreferences),
            ResourceUtils.ResourceGroup("layout", *layouts),
            ResourceUtils.ResourceGroup("drawable", "biliroaming_bg_transparent.webp")
        ).forEach {
            context.copyResources("bilibili", it)
        }
        context.mergeStrings("bilibili/host/values/strings.xml")
        context.mergeStrings("bilibili/host/values/strings_raw.xml")
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
