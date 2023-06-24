package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("modify-modifier")
@Description("修改类或类成员修饰符，方便操作")
@Version("0.0.1")
class ModifyModifierPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$TabResponse;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$TabData;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
        context.findClass("Ltv/danmaku/bili/ui/main2/resource/MainResourceManager\$Tab;")
            ?.mutableClass?.accessFlags = AccessFlags.PUBLIC.value
        return PatchResultSuccess()
    }
}
