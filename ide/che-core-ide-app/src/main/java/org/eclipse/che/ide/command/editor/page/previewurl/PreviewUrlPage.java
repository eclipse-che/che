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
package org.eclipse.che.ide.command.editor.page.previewurl;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.command.editor.page.editable.AbstractPageWithEditor;
import org.eclipse.che.ide.command.editor.page.editable.PageWithEditorView;
import org.eclipse.che.ide.command.editor.page.editable.editor.MacroEditorConfiguration;
import org.eclipse.che.ide.macro.chooser.MacroChooser;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * {@link CommandEditorPage} which allows to edit command's preview URL.
 *
 * @author Artem Zatsarynnyi
 */
public class PreviewUrlPage extends AbstractPageWithEditor {

    @Inject
    public PreviewUrlPage(PageWithEditorView view,
                          EditorBuilder editorBuilder,
                          FileTypeRegistry fileTypeRegistry,
                          MacroChooser macroChooser,
                          EditorMessages messages,
                          MacroEditorConfiguration editorConfiguration) {
        super(view,
              editorBuilder,
              fileTypeRegistry,
              macroChooser,
              messages.pagePreviewUrlTitle(),
                messages.pagePreviewUrlTooltip(),
                editorConfiguration);
    }

    @Override
    protected String getCommandPropertyValue() {
        final String previewUrl = editedCommand.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);

        return previewUrl != null ? previewUrl : "";
    }

    @Override
    protected void updateCommandPropertyValue(String content) {
        editedCommand.getAttributes().put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, content);
    }
}
