package org.ffpy.plugin.coding.util

import com.intellij.designer.clipboard.SimpleTransferable
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.DataFlavor

object CopyPasteUtils {

    fun putString(content: String?) {
        CopyPasteManager.getInstance().setContents(SimpleTransferable(content, DataFlavor.stringFlavor))
    }

    fun getString(): String? {
        return CopyPasteManager.getInstance().contents?.let {
            if (it.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return try {
                    it.getTransferData(DataFlavor.stringFlavor) as String
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
}