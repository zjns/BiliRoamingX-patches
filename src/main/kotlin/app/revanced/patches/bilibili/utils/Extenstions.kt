package app.revanced.patches.bilibili.utils

import app.revanced.patcher.extensions.or
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.HiddenApiRestriction
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.immutable.*
import org.jf.dexlib2.immutable.debug.ImmutableDebugItem
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction

internal fun Method.clone(
    registerCount: Int = implementation?.registerCount ?: 0,
    clearImplementation: Boolean = false,
    name: String = this.name,
    accessFlags: Int = this.accessFlags,
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

internal fun Method.cloneMutable(
    registerCount: Int = implementation?.registerCount ?: 0,
    clearImplementation: Boolean = false,
    name: String = this.name,
    accessFlags: Int = this.accessFlags,
) = clone(registerCount, clearImplementation, name, accessFlags).toMutable()

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

fun methodImplementation(
    registerCount: Int,
    instructions: List<ImmutableInstruction>? = null,
    tryBlocks: List<ImmutableTryBlock>? = null,
    debugItems: List<ImmutableDebugItem>? = null
) = ImmutableMethodImplementation(registerCount, instructions, tryBlocks, debugItems)
