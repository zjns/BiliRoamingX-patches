package app.revanced.patches.bilibili.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("tv.danmaku.bili")])
@Target(AnnotationTarget.CLASS)
internal annotation class BiliBiliCompatibility
