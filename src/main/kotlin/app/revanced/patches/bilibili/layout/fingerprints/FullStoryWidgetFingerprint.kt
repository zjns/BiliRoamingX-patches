package app.revanced.patches.bilibili.layout.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

class FullStoryWidgetFingerprint(tagName: String) : MethodFingerprint(
    strings = listOf(tagName, "player.player.story-button.0.player")
)
