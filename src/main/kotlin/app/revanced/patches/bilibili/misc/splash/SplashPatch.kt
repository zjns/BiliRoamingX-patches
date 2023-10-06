package app.revanced.patches.bilibili.misc.splash

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.darkThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.lightThemeBackgroundColor
import org.w3c.dom.Element

@Patch(
    name = "Splash",
    description = "闪屏页背景色跟随深色模式",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
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
            val layerListNode = editor.file.getElementsByTagName("layer-list").item(0) as Element
            val children = layerListNode.childNodes
            out@ for (i in 0 until children.length) {
                val item = children.item(i) as? Element ?: continue
                val itemChildren = item.childNodes
                for (j in 0 until itemChildren.length) {
                    val itemChild = itemChildren.item(j) as? Element ?: continue
                    if (itemChild.tagName == "color") {
                        itemChild.setAttribute("android:color", "@color/$COLOR_NAME_BILIROAMING_BG_SPLASH")
                        break@out
                    }
                }
            }
        }
        context.xmlEditor["res/layout/bili_app_layout_brand_splash_fragment.xml"].use { editor ->
            val rootNode = editor.file.getElementsByTagName(
                "androidx.constraintlayout.widget.ConstraintLayout"
            ).item(0) as Element
            rootNode.setAttribute("android:background", "@color/$COLOR_NAME_BILIROAMING_BG_SPLASH")
        }
    }

    private fun addResourceStyleItem(
        context: ResourceContext,
        resourceFile: String,
        styleName: String,
        name: String,
        value: String,
    ) = context.xmlEditor[resourceFile].use { editor ->
        val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element
        val children = resourcesNode.childNodes
        for (i in 0 until children.length) {
            val style = children.item(i) as? Element ?: continue
            if (style.tagName == "style" && style.getAttribute("name") == styleName) {
                editor.file.createElement("item").apply {
                    setAttribute("name", name)
                    textContent = value
                }.also { style.appendChild(it) }
                break
            }
        }
    }

    private fun addResourceColor(
        context: ResourceContext,
        resourceFile: String,
        name: String,
        value: String
    ) = context.xmlEditor[resourceFile].use {
        val resourcesNode = it.file.getElementsByTagName("resources").item(0) as Element
        resourcesNode.appendChild(
            it.file.createElement("color").apply {
                setAttribute("name", name)
                textContent = value
            })
    }

    private const val STYLE_NAME_APP_THEME = "AppTheme"
    private const val STYLE_ITEM_NAME_SPLASH_BG = "android:windowSplashScreenBackground"
    private const val COLOR_NAME_BILIROAMING_BG_SPLASH = "biliroaming_bg_splash"
}
