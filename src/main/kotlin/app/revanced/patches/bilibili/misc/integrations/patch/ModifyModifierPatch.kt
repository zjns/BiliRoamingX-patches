package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import com.android.tools.smali.dexlib2.AccessFlags

@Patch(
    name = "Modify modifier",
    description = "修改类或类成员修饰符，方便操作",
    compatiblePackages = [CompatiblePackage(name = "tv.danmaku.bili"), CompatiblePackage(name = "tv.danmaku.bilibilihd")]
)
object ModifyModifierPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$TabResponse;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$TabData;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$Tab;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
    }
}
