package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.utils.cloneMutable
import com.android.tools.smali.dexlib2.AccessFlags

@Patch(
    name = "Lib bili",
    description = "查找网络请求签名及获取appKey方法",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object LibBiliPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        val libBiliClass = context.findClass("Lcom/bilibili/nativelibrary/LibBili;")
            ?: throw PatchException("not found LibBili class")
        val getAppKeyMethod = libBiliClass.immutableClass.methods.find { m ->
            AccessFlags.PUBLIC.isSet(m.accessFlags)
                    && !AccessFlags.NATIVE.isSet(m.accessFlags)
                    && m.returnType == "Ljava/lang/String;"
                    && m.parameterTypes.size == 1
                    && m.parameterTypes[0] == "Ljava/lang/String;"
        } ?: throw PatchException("not found getAppKey method")
        val signQueryMethod = libBiliClass.immutableClass.methods.find { m ->
            AccessFlags.PUBLIC.isSet(m.accessFlags)
                    && !AccessFlags.NATIVE.isSet(m.accessFlags)
                    && m.parameterTypes.size == 1
                    && m.parameterTypes[0] == "Ljava/util/Map;"
        } ?: throw PatchException("not found signQuery method")
        val utilsClass = context.findClass("Lapp/revanced/bilibili/utils/Utils;")!!
        val utilsGetMobiAppMethod = utilsClass.mutableClass.methods.first { it.name == "getMobiApp" }
        utilsClass.mutableClass.methods.run {
            first { it.name == "getAppKey" }.also { remove(it) }
                .cloneMutable(registerCount = 1, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {}, $utilsGetMobiAppMethod
                        move-result-object v0
                        invoke-static {v0}, $getAppKeyMethod
                        move-result-object v0
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
            first { it.name == "signQuery" }.also { remove(it) }
                .cloneMutable(registerCount = 2, clearImplementation = true).apply {
                    addInstructions(
                        """
                        invoke-static {p0}, $signQueryMethod
                        move-result-object v0
                        invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;
                        move-result-object v0
                        return-object v0
                    """.trimIndent()
                    )
                }.also { add(it) }
        }
    }
}
