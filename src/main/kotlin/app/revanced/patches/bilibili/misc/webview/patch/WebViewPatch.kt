package app.revanced.patches.bilibili.misc.webview.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.bilibili.annotations.BiliBiliCompatibility
import app.revanced.util.patch.AbstractTransformInstructionsPatch
import app.revanced.util.patch.IMethodCall
import app.revanced.util.patch.Instruction35cInfo
import app.revanced.util.patch.filterMapInstruction35c
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction

@Patch
@BiliBiliCompatibility
@Name("webview-patch")
@Description("WebView 补丁")
internal class WebViewPatch : AbstractTransformInstructionsPatch<Instruction35cInfo>() {
    enum class MethodCall(
        override val definedClassName: String,
        override val methodName: String,
        override val methodParams: Array<String>,
        override val returnType: String
    ) : IMethodCall {
        SetWebViewClient(
            "Landroid/webkit/WebView;",
            "setWebViewClient",
            arrayOf("Landroid/webkit/WebViewClient;"),
            "V"
        ),
    }

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ) = filterMapInstruction35c<MethodCall>(
        INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX,
        classDef,
        instruction,
        instructionIndex
    )

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (methodType, instruction, instructionIndex) = entry
        methodType.replaceInvokeVirtualWithIntegrations(
            INTEGRATIONS_CLASS_DESCRIPTOR,
            mutableMethod,
            instruction,
            instructionIndex
        )
    }

    companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
            "Lapp/revanced/bilibili/patches/webview/WebViewPatch"
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"
    }
}
