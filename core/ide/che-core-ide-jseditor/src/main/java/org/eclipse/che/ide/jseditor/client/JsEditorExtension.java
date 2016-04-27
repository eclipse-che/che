/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.jseditor.client.inject.PlainTextFileType;
import org.eclipse.che.ide.jseditor.client.popup.PopupResources;
import org.eclipse.che.ide.jseditor.client.texteditor.EditorResources;

@Extension(title = "Common Editor", version = "3.1.0")
public class JsEditorExtension {

    @Inject
    public JsEditorExtension(final FileTypeRegistry fileTypeRegistry,
                             final @PlainTextFileType FileType plainText,
                             final EditorResources editorResources,
                             final PopupResources popupResources) {
        // register text/plain file type
        fileTypeRegistry.registerFileType(plainText);

        // ensure css injection
        editorResources.editorCss().ensureInjected();
        popupResources.popupStyle().ensureInjected();
    }
}
