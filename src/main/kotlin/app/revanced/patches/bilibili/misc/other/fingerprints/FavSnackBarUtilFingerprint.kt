package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object FavSnackBarUtilFingerprint : MethodFingerprint(
    strings = listOf("ff_player_fav_new"),
    returnType = "Ljava/lang/Boolean;"
)
