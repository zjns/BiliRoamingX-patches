package app.revanced.patches.bilibili.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch.IntegrationsFingerprint

object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/bilibili/gripper/BiliApp;" && methodDef.name == "onCreate"
    }
)
