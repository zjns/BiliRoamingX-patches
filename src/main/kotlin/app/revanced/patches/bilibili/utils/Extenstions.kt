package app.revanced.patches.bilibili.utils

import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.bilibili.patcher.fingerprint.MultiMethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.HiddenApiRestriction
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MethodParameter
import org.jf.dexlib2.immutable.*
import org.jf.dexlib2.immutable.debug.ImmutableDebugItem
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue
import org.jf.dexlib2.immutable.value.ImmutableEncodedValue
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue

fun Method.clone(
    registerCount: Int = implementation?.registerCount ?: 0,
    clearImplementation: Boolean = false,
    name: String = this.name,
    accessFlags: Int = this.accessFlags,
    parameters: List<MethodParameter> = this.parameters,
): ImmutableMethod {
    val clonedImplementation = implementation?.let {
        ImmutableMethodImplementation(
            registerCount,
            if (clearImplementation) emptyList() else it.instructions,
            if (clearImplementation) emptyList() else it.tryBlocks,
            if (clearImplementation) emptyList() else it.debugItems,
        )
    }
    return ImmutableMethod(
        definingClass,
        name,
        parameters,
        returnType,
        accessFlags,
        annotations,
        hiddenApiRestrictions,
        clonedImplementation
    )
}

fun Method.cloneMutable(
    registerCount: Int = implementation?.registerCount ?: 0,
    clearImplementation: Boolean = false,
    name: String = this.name,
    accessFlags: Int = this.accessFlags,
    parameters: List<MethodParameter> = this.parameters,
) = clone(registerCount, clearImplementation, name, accessFlags, parameters).toMutable()

fun Int.toPublic() = or(AccessFlags.PUBLIC).and(AccessFlags.PRIVATE.value.inv())
fun Int.removeFinal() = and(AccessFlags.FINAL.value.inv())

fun method(
    definingClass: String,
    name: String,
    returnType: String,
    accessFlags: Int,
    parameters: List<ImmutableMethodParameter>? = null,
    annotations: Set<ImmutableAnnotation>? = null,
    hiddenApiRestrictions: Set<HiddenApiRestriction>? = null,
    implementation: ImmutableMethodImplementation? = null
) = ImmutableMethod(
    definingClass,
    name,
    parameters,
    returnType,
    accessFlags,
    annotations,
    hiddenApiRestrictions,
    implementation
)

fun methodParameter(
    type: String,
    name: String? = null,
    annotations: Set<ImmutableAnnotation>? = null
) = ImmutableMethodParameter(type, annotations, name)

val String.encodedValue: ImmutableStringEncodedValue
    get() = stringEncodedValue(this)

fun stringEncodedValue(value: String) = ImmutableStringEncodedValue(value)

fun arrayEncodedValue(
    value: List<ImmutableEncodedValue>
) = ImmutableArrayEncodedValue(value)

fun annotationElement(
    name: String,
    value: ImmutableEncodedValue
) = ImmutableAnnotationElement(name, value)

fun annotation(
    visibility: Int,
    type: String,
    elements: Set<ImmutableAnnotationElement>
) = ImmutableAnnotation(visibility, type, elements)

fun methodImplementation(
    registerCount: Int,
    instructions: List<ImmutableInstruction>? = null,
    tryBlocks: List<ImmutableTryBlock>? = null,
    debugItems: List<ImmutableDebugItem>? = null
) = ImmutableMethodImplementation(registerCount, instructions, tryBlocks, debugItems)

fun MultiMethodFingerprint.toErrorResult() = PatchResultError("Failed to resolve ${javaClass.simpleName}")

val String.className: String
    get() {
        return if (startsWith("L") && endsWith(";"))
            substring(1, length - 1).replace('/', '.')
        else replace('/', '.')
    }
