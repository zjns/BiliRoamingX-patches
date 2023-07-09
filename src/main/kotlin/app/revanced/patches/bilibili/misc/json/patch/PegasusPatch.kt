package app.revanced.patches.bilibili.misc.json.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.json.fingerprints.CardClickProcessorFingerprint
import app.revanced.patches.bilibili.misc.json.fingerprints.PegasusParserFingerprint
import app.revanced.patches.bilibili.utils.cloneMutable
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Field
import org.jf.dexlib2.iface.value.StringEncodedValue

@Patch
@BiliBiliCompatibility
@Name("pegasus-hook")
@Description("首页推荐流hook")
@Version("0.0.1")
class PegasusPatch : BytecodePatch(listOf(PegasusParserFingerprint, CardClickProcessorFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        PegasusParserFingerprint.result?.run {
            val method = mutableClass.methods.first { it.returnType == "Lcom/bilibili/okretro/GeneralResponse;" }
            method.cloneMutable(registerCount = 2, clearImplementation = true).apply {
                method.name = method.name + "_Origin"
                addInstructions(
                    """
                    invoke-virtual {p0, p1}, $method
                    move-result-object p1
                    invoke-static {p1}, Lapp/revanced/bilibili/patches/json/PegasusPatch;->pegasusHook(Lcom/bilibili/okretro/GeneralResponse;)V
                    return-object p1
                """.trimIndent()
                )
            }.also {
                mutableClass.methods.add(it)
            }
        } ?: return PegasusParserFingerprint.toErrorResult()
        var bannerItemFiled: Field? = null
        var stockBannersItemClass: ClassDef? = null
        for (classDef in context.classes) {
            if (classDef.superclass == "Lcom/bilibili/pegasus/api/model/BasicIndexItem;") {
                val field = classDef.fields.find { f ->
                    f.annotations.find { a ->
                        a.type == "Lcom/alibaba/fastjson/annotation/JSONField;" && a.elements.find { e ->
                            e.name == "name" && e.value.let {
                                it is StringEncodedValue && it.value == "banner_item"
                            }
                        } != null
                    } != null
                }
                if (field != null) {
                    stockBannersItemClass = classDef
                    bannerItemFiled = field
                    break
                }
            }
        }
        if (bannerItemFiled == null || stockBannersItemClass == null)
            return PatchResultError("not found banner item field")
        val myBannersItemClassName = "Lapp/revanced/bilibili/meta/pegasus/BannersItem;"
        val myBannersItemClass = context.findClass(myBannersItemClassName)!!
        context.proxy(stockBannersItemClass).mutableClass.setSuperClass(myBannersItemClassName)
        myBannersItemClass.mutableClass.methods.run {
            find { it.name == "getBanners" }?.also { remove(it) }
                ?.cloneMutable(3, clearImplementation = true)
                ?.apply {
                    addInstructions(
                        """
                        move-object v0, p0
                        check-cast v0, $stockBannersItemClass
                        iget-object v1, v0, $bannerItemFiled
                        return-object v1
                    """.trimIndent()
                    )
                }?.also { add(it) }
        }
        CardClickProcessorFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0, """
            invoke-static {p3}, Lapp/revanced/bilibili/patches/json/PegasusPatch;->onFeedClick(Lcom/bilibili/app/comm/list/common/data/DislikeReason;)Z
            move-result v0
            if-eqz v0, :jump
            return-void
            :jump
            nop
        """.trimIndent()
        ) ?: return CardClickProcessorFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
