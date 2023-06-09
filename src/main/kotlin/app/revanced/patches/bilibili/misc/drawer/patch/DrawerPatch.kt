package app.revanced.patches.bilibili.misc.drawer.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.drawer.fingerprints.DrawerControlFingerprint
import app.revanced.patches.bilibili.misc.drawer.fingerprints.DrawerIsOpenFingerprint
import app.revanced.patches.bilibili.misc.drawer.fingerprints.DrawerLayoutParamsFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.AccessFlags

@Patch
@BiliBiliCompatibility
@Name("drawer-patch")
@Description("我的页面移至侧滑栏辅助补丁")
@Version("0.0.1")
class DrawerPatch(
    private val openDrawer: MethodFingerprint = DrawerControlFingerprint(true),
    private val closeDrawer: MethodFingerprint = DrawerControlFingerprint(false)
) : BytecodePatch(listOf(openDrawer, closeDrawer, DrawerIsOpenFingerprint, DrawerLayoutParamsFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val drawerExClass = context.findClass("Lapp/revanced/bilibili/patches/drawer/DrawerLayoutEx;")
        val openMethod = openDrawer.result?.method
            ?: return PatchResultError("not found openDrawer method")
        val closeMethod = closeDrawer.result?.method
            ?: return PatchResultError("not found closeDrawer method")
        val isOpenMethod = DrawerIsOpenFingerprint.result?.method
            ?: return PatchResultError("not found isDrawerOpen method")
        drawerExClass!!.mutableClass.methods.run {
            first { it.name == "openDrawerEx" }.addInstruction(
                0, "invoke-virtual {p0, p1, p2}, $openMethod"
            )
            first { it.name == "closeDrawerEx" }.addInstruction(
                0, "invoke-virtual {p0, p1, p2}, $closeMethod"
            )
            first { it.name == "isDrawerOpenEx" }.also { remove(it) }
                .cloneMutable(2, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-virtual {p0, p1}, $isOpenMethod
                        move-result p1
                        return p1
                    """.trimIndent()
                    )
                }.also { add(it) }
        }
        val layoutParamsExClass =
            context.findClass("Lapp/revanced/bilibili/patches/drawer/DrawerLayoutEx\$LayoutParamsEx;")!!
        val gravityField = DrawerLayoutParamsFingerprint.result?.classDef?.fields?.first {
            it.type == "I" && it.accessFlags == AccessFlags.PUBLIC.value
        } ?: return PatchResultError("not found gravity field")
        layoutParamsExClass.mutableClass.setSuperClass(gravityField.definingClass)
        layoutParamsExClass.mutableClass.methods.run {
            first { it.name == "<init>" }.replaceInstruction(
                0, """
                invoke-direct {p0, p1, p2}, ${gravityField.definingClass}-><init>(II)V
            """.trimIndent()
            )
            first { it.name == "setGravityEx" }.addInstruction(
                0, """
                iput p1, p0, $gravityField
            """.trimIndent()
            )
        }
        context.findClass("Ltv/danmaku/bili/ui/main2/basic/BaseMainFrameFragment;")
            ?.mutableClass?.methods?.find { it.name == "onViewCreated" }?.run {
                val insertIdx = implementation!!.instructions.size - 1
                addInstructions(
                    insertIdx, """
                invoke-virtual {p0}, Landroidx/fragment/app/Fragment;->getView()Landroid/view/View;
                move-result-object p1
                invoke-static {p1}, Lapp/revanced/bilibili/patches/drawer/DrawerPatch;->onMainFrameFragmentViewCreated(Landroid/view/View;)V
                """.trimIndent()
                )
            } ?: return PatchResultError("can not found BaseMainFrameFragment")
        return PatchResultSuccess()
    }
}
