package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.children
import app.revanced.util.get
import app.revanced.util.insertBefore
import app.revanced.util.set

@Patch(
    name = "Play upgrade",
    description = "Play版注入安装apk权限补丁",
    compatiblePackages = [CompatiblePackage(name = "com.bilibili.app.in")]
)
object PlayUpgradePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.xmlEditor["AndroidManifest.xml"].use { dom ->
            val manifest = dom["manifest"]
            val permTag = "uses-permission"
            val nameAttr = "android:name"
            val permName = "android.permission.REQUEST_INSTALL_PACKAGES"
            if (manifest.children().none { it.tagName == permTag && it[nameAttr] == permName }) {
                manifest.insertBefore(dom["application"], permTag) {
                    this[nameAttr] = permName
                }
            }
        }
    }
}
