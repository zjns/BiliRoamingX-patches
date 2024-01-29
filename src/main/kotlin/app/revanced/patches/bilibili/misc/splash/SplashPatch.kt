package app.revanced.patches.bilibili.misc.splash

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.utils.children
import app.revanced.patches.bilibili.utils.get
import app.revanced.patches.bilibili.utils.set
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.darkThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.lightThemeBackgroundColor

@Patch(
    name = "Splash",
    description = "闪屏页背景色跟随深色模式",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object SplashPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        addResourceStyleItem(
            context, "res/values/styles.xml",
            STYLE_NAME_APP_THEME, STYLE_ITEM_NAME_SPLASH_BG, lightThemeBackgroundColor!!
        )
        addResourceStyleItem(
            context, "res/values-night/styles.xml",
            STYLE_NAME_APP_THEME, STYLE_ITEM_NAME_SPLASH_BG, darkThemeBackgroundColor!!
        )
        addResourceColor(
            context, "res/values/colors.xml",
            COLOR_NAME_BILIROAMING_BG_SPLASH, lightThemeBackgroundColor!!
        )
        addResourceColor(
            context, "res/values-night/colors.xml",
            COLOR_NAME_BILIROAMING_BG_SPLASH, darkThemeBackgroundColor!!
        )
        context.xmlEditor["res/drawable/layerlist_splash.xml"].use { editor ->
            editor.file["layer-list"].children().flatMap { it.children() }
                .first { it.tagName == "color" }["android:color"] = "@color/$COLOR_NAME_BILIROAMING_BG_SPLASH"
        }
        context.xmlEditor["res/drawable/safe_mode_layerlist_splash.xml"].use { editor ->
            editor.file["layer-list"].children().flatMap { it.children() }
                .first { it.tagName == "color" }["android:color"] = "@color/$COLOR_NAME_BILIROAMING_BG_SPLASH"
        }
        context.xmlEditor["res/layout/bili_app_layout_brand_splash_fragment.xml"].use { editor ->
            editor.file["androidx.constraintlayout.widget.ConstraintLayout"]["android:background"] =
                "@color/$COLOR_NAME_BILIROAMING_BG_SPLASH"
        }
    }

    private fun addResourceStyleItem(
        context: ResourceContext,
        resourceFile: String,
        styleName: String,
        name: String,
        value: String,
    ) = context.xmlEditor[resourceFile].use { editor ->
        editor.file["resources"].children().find {
            it.tagName == "style" && it["name"] == styleName
        }?.let { style ->
            editor.file.createElement("item").apply {
                this["name"] = name
                textContent = value
            }.also { style.appendChild(it) }
        }
    }

    private fun addResourceColor(
        context: ResourceContext,
        resourceFile: String,
        name: String,
        value: String
    ) = context.xmlEditor[resourceFile].use {
        it.file["resources"].appendChild(
            it.file.createElement("color").apply {
                this["name"] = name
                textContent = value
            })
    }

    private const val STYLE_NAME_APP_THEME = "AppTheme"
    private const val STYLE_ITEM_NAME_SPLASH_BG = "android:windowSplashScreenBackground"
    private const val COLOR_NAME_BILIROAMING_BG_SPLASH = "biliroaming_bg_splash"
}
