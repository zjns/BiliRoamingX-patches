package app.revanced.patches.bilibili.misc.network

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch

@Patch(
    name = "Override certificate pinning",
    description = "Overrides certificate pinning, allowing to inspect traffic via a proxy.",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object OverrideCertificatePinningPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context["res/xml/network_security_config.xml"].apply {
            delete()
            createNewFile()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <network-security-config>
                    <base-config cleartextTrafficPermitted="true">
                        <trust-anchors>
                            <certificates src="system" />
                            <certificates
                                src="user"
                                overridePins="true" />
                        </trust-anchors>
                    </base-config>
                    <debug-overrides>
                        <trust-anchors>
                            <certificates src="system" />
                            <certificates
                                src="user"
                                overridePins="true" />
                        </trust-anchors>
                    </debug-overrides>
                </network-security-config>
                """.trimIndent()
            )
        }
    }
}
