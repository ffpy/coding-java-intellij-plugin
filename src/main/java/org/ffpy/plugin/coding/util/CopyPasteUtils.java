package org.ffpy.plugin.coding.util;

import com.intellij.designer.clipboard.SimpleTransferable;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;

public class CopyPasteUtils {

    public static void putString(String content) {
        CopyPasteManager.getInstance().setContents(new SimpleTransferable(content, DataFlavor.stringFlavor));
    }

    public static Optional<String> getString() {
        return Optional.ofNullable(CopyPasteManager.getInstance().getContents())
                .filter(contents -> contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                .map(contents -> {
                    try {
                        return (String) contents.getTransferData(DataFlavor.stringFlavor);
                    } catch (UnsupportedFlavorException | IOException e) {
                        return null;
                    }
                });
    }
}
