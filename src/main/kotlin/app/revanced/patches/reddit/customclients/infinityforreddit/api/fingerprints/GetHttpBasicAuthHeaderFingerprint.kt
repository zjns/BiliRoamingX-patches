package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

object GetHttpBasicAuthHeaderFingerprint : AbstractClientIdFingerprint(additionalStrings = arrayOf("Authorization"))