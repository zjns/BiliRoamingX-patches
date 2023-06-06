package app.revanced.patches.bilibili.utils

import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation

internal fun Method.clone(registerCount: Int = 0): ImmutableMethod {
    val clonedImplementation = implementation?.let {
        ImmutableMethodImplementation(
            it.registerCount + registerCount,
            it.instructions,
            it.tryBlocks,
            it.debugItems,
        )
    }
    return ImmutableMethod(
        returnType, name, parameters, returnType, accessFlags, annotations, hiddenApiRestrictions, clonedImplementation
    )
}

internal fun Method.cloneMutable(registerCount: Int = 0) = clone(registerCount).toMutable()

