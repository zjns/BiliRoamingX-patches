package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object PivotBarCreateButtonViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_DIRECT_RANGE, // unique instruction anchor
    )
)