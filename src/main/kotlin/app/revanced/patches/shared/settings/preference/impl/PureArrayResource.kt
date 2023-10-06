package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

class ArrayItem(val value: String)

/**
 *  An array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 */
class PureArrayResource(
    name: String,
    val items: List<ArrayItem>,
) : BaseResource(name, "string-array") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("name", name)
            items.forEach { item ->
                this.appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = item.value
                })
            }
        }
}
