package app.revanced.patches.shared.misc.settings.preference

import app.revanced.patches.shared.misc.settings.preference.IntentPreference.Intent
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference that opens an intent.
 *
 * @param key The preference key. If null, other parameters must be specified.
 * @param titleKey The preference title key.
 * @param summaryKey The preference summary key.
 * @param tag The preference tag.
 * @param intent The intent to open.
 *
 * @see Intent
 */
class IntentPreference(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    tag: String = "Preference",
    val intent: Intent,
) : BasePreference(null, titleKey, summaryKey, tag) {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            appendChild(ownerDocument.createElement("intent").also { intentNode ->
                intentNode.setAttribute("android:data", intent.data)
                intentNode.setAttribute("android:targetClass", intent.targetClass)
                intentNode.setAttribute("android:targetPackage", intent.targetPackageSupplier())
            })
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IntentPreference

        return intent == other.intent
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + intent.hashCode()
        return result
    }

    data class Intent(
        internal val data: String,
        internal val targetClass: String,
        internal val targetPackageSupplier: () -> String,
    )
}
