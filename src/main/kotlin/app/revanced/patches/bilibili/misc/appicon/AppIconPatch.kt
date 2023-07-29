package app.revanced.patches.bilibili.misc.appicon

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources

@Patch
@BiliBiliCompatibility
@Name("app-icon")
@Description("恢复APP图标")
class AppIconPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        val iconFiles = arrayOf(
            "ic_launcher.png",
            "ic_launcher_background.png",
            "ic_launcher_foreground.png",
            "ic_launcher_foreground_round.png",
            "ic_launcher_round.png",
            "ic_launcher_monochrome.png"
        )
        val iconPaths = arrayOf(
            "mipmap-mdpi",
            "mipmap-hdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi"
        )
        iconPaths.forEach {
            ResourceUtils.ResourceGroup(it, *iconFiles).run {
                context.copyResources("bilibili/appicon", this)
            }
        }
        ResourceUtils.ResourceGroup(
            "mipmap-anydpi-v26", "ic_launcher.xml", "ic_launcher_round.xml"
        ).run {
            context.copyResources("bilibili/appicon", this)
        }
        return PatchResultSuccess()
    }
}
