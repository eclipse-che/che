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
package org.eclipse.che.ide.command.editor.page.arguments;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.command.editor.page.editable.AbstractPageWithEditor;
import org.eclipse.che.ide.command.editor.page.editable.PageWithEditorView;
import org.eclipse.che.ide.macro.chooser.MacroChooser;

/**
 * {@link CommandEditorPage} which allows to edit command's command line.
 *
 * @author Artem Zatsarynnyi
 */
public class ArgumentsPage extends AbstractPageWithEditor {

    @Inject
    public ArgumentsPage(PageWithEditorView view,
                         EditorBuilder editorBuilder,
                         FileTypeRegistry fileTypeRegistry,
                         MacroChooser macroChooser,
                         EditorMessages messages) {
        super(view,
              editorBuilder,
              fileTypeRegistry,
              macroChooser,
              messages.pageArgumentsTitle(),
              messages.pageArgumentsTooltip());
    }

    @Override
    protected String getCommandPropertyValue() {
        return editedCommand.getCommandLine();
    }

    @Override
    protected void updateCommandPropertyValue(String content) {
        editedCommand.setCommandLine(content);
    }
}
