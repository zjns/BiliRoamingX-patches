package app.revanced.patches.bilibili.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patches.bilibili.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.bilibili.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch

@Patch
@Name("integrations")
@IntegrationsCompatibility
@RequiresIntegrations
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/bilibili/utils/Utils;",
    listOf(InitFingerprint)
)
