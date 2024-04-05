package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.utils.*
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.value.IntEncodedValue

@Patch(
    name = "Cache redirect",
    description = "详情页三点缓存菜单重定向，允许外部工具直接下载",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ],
    dependencies = [ResourceMappingPatch::class]
)
object CacheRedirectPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        val dialogMenuLayoutId = ResourceMappingPatch.resourceMappings.first {
            it.type == "layout" && it.name == "bili_app_list_item_super_menu_dialog_menu"
        }.id.toInt()
        val dialogMenuLayoutIdField = context.classes.firstNotNullOf { c ->
            c.fields.find { f ->
                f.accessFlags.let { it.isPublic() && it.isStatic() }
                        && f.type == "I" && (f.initialValue as? IntEncodedValue)?.value == dialogMenuLayoutId
            }
        }
        context.classes.firstNotNullOf { c ->
            if (c.methods.asSequence().filterNot { m ->
                    m.accessFlags.let { it.isAbstract() || it.isNative() }
                }.any { m ->
                    m.implementation!!.instructions.any {
                        it.opcode == Opcode.SGET && (it as ReferenceInstruction).reference == dialogMenuLayoutIdField
                    }
                }) c else null
        }.let { c ->
            val onClickOriginListenerType = "Lapp/revanced/bilibili/widget/OnClickOriginListener;"
            context.proxy(c).mutableClass.run {
                interfaces.add(onClickOriginListenerType)
                val originOnClickMethod = methods.first {
                    it.name == "onClick" && it.parameterTypes == listOf("Landroid/view/View;") && it.returnType == "V"
                }
                originOnClickMethod.cloneMutable(registerCount = 2, clearImplementation = true).apply {
                    originOnClickMethod.name += "_Origin"
                    addInstructions(
                        0, """
                        invoke-static {p1, p0}, Lapp/revanced/bilibili/patches/CacheRedirectPatch;->onMenuClick(Landroid/view/View;$onClickOriginListenerType)V
                        return-void
                    """.trimIndent()
                    )
                }.also { methods.add(it) }
            }
        }
    }
}
