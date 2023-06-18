package app.revanced.patches.bilibili.misc.json.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.patches.bilibili.misc.json.fingerprints.JSONFingerprint
import org.jf.dexlib2.Opcode

@Patch
@BiliBiliCompatibility
@Name("json-patch")
@Description("通用阿里Fastjson反序列化数据修改")
@Version("0.0.1")
class JSONPatch : BytecodePatch(listOf(JSONFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        val clazz = JSONFingerprint.result?.mutableClass
            ?: return JSONFingerprint.toErrorResult()
        val parseObject = if (clazz.type.endsWith("/JSON;")) "parseObject" else "a"
        clazz.methods.find { m ->
            m.name == parseObject && m.parameterTypes.let {
                it.size == 4 && it[0] == "Ljava/lang/String;"
                        && it[1] == "Ljava/lang/reflect/Type;"
                        && it[2] == "I" && it[3] == "[Lcom/alibaba/fastjson/parser/Feature;"
            }
        }?.run {
            val insetIndex = implementation?.instructions?.indexOfLast {
                it.opcode == Opcode.RETURN_OBJECT
            }.takeUnless { it == -1 } ?: 0
            addInstructions(
                insetIndex, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/json/JSONPatch;->parseObjectHook(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object p0
            """.trimIndent()
            )
        }
        clazz.methods.find { m ->
            m.name == parseObject && m.parameterTypes.let {
                it.size == 3 && it[0] == "Ljava/lang/String;"
                        && it[1] == "Ljava/lang/reflect/Type;"
                        && it[2] == "[Lcom/alibaba/fastjson/parser/Feature;"
            }
        }?.run {
            val insetIndex = implementation?.instructions?.indexOfLast {
                it.opcode == Opcode.RETURN_OBJECT
            }.takeUnless { it == -1 } ?: 0
            addInstructions(
                insetIndex, """
                invoke-static {p0}, Lapp/revanced/bilibili/patches/json/JSONPatch;->parseObjectHook(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object p0
            """.trimIndent()
            )
        }
        clazz.methods.find { m ->
            m.name == "parseArray" && m.parameterTypes.let {
                it.size == 2 && it[0] == "Ljava/lang/String;" && it[1] == "Ljava/lang/Class;"
            }
        }?.run {
            val insetIndex = implementation?.instructions?.indexOfLast {
                it.opcode == Opcode.RETURN_OBJECT
            }.takeUnless { it == -1 } ?: 0
            addInstructions(
                insetIndex, """
                invoke-static {p1, v0}, Lapp/revanced/bilibili/patches/json/JSONPatch;->parseArrayHook(Ljava/lang/Class;Ljava/util/ArrayList;)V
            """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}
