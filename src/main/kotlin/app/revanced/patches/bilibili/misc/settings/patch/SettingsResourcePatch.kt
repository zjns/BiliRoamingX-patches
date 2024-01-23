package app.revanced.patches.bilibili.misc.settings.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.AbstractSettingsResourcePatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeArrays
import app.revanced.util.resources.ResourceUtils.mergeStrings

@Patch(
    name = "Settings resource patch",
    description = "哔哩漫游设置入口",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd"), CompatiblePackage(name = "com.bilibili.app.in")],
    dependencies = [ResourceMappingPatch::class]
)
object SettingsResourcePatch : AbstractSettingsResourcePatch(
    "biliroaming_settings", "bilibili", false
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
        "biliroaming_setting_block_module.xml",
        "biliroaming_setting_backup.xml",
        "biliroaming_setting_player_version.xml",
        "biliroaming_setting_display_size.xml",
        "biliroaming_setting_filter_popular.xml",
        "biliroaming_setting_filter_comment.xml",
    )
    private val layouts = arrayOf(
        "biliroaming_dialog_argb_color_choose.xml",
        "biliroaming_dialog_customize_backup.xml",
        "biliroaming_dialog_color_choose.xml",
    )

    override fun execute(context: ResourceContext) {
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
