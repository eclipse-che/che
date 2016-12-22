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

package org.eclipse.che.ide.command.editor.page.editable;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.macro.chooser.MacroChooser;
import org.eclipse.che.ide.macro.chooser.MacroChooser.MacroChosenCallback;

import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_DIRTY;
import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_INPUT;

/**
 * Abstract {@link CommandEditorPage} that allows to edit a command's property
 * with Orion editor with ability to explore and insert macros.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractPageWithEditor extends AbstractCommandEditorPage implements PageWithEditorView.ActionDelegate {

    private final PageWithEditorView view;
    private final FileTypeRegistry   fileTypeRegistry;
    private final MacroChooser       macroChooser;

    private TextEditor editor;

    /** Initial value of the edited command's property. */
    private String initialValue;

    protected AbstractPageWithEditor(PageWithEditorView view,
                                     EditorBuilder editorBuilder,
                                     FileTypeRegistry fileTypeRegistry,
                                     MacroChooser macroChooser,
                                     String title,
                                     String tooltip) {
        super(title, tooltip);

        this.view = view;
        this.fileTypeRegistry = fileTypeRegistry;
        this.macroChooser = macroChooser;

        view.setDelegate(this);

        initializeEditor(editorBuilder);
    }

    private void initializeEditor(EditorBuilder editorBuilder) {
        editor = editorBuilder.buildEditor();

        editor.activate();

        editor.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                switch (propId) {
                    case PROP_INPUT:
                        editor.go(view.getEditorContainer());

                        break;
                    case PROP_DIRTY:
                        updateCommandPropertyValue(editor.getDocument().getContents());
                        notifyDirtyStateChanged();

                        break;
                    default:
                }
            }
        });
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        initialValue = getCommandPropertyValue();

        setContent(initialValue);
    }

    /** Sets editor's content. */
    private void setContent(String content) {
        final VirtualFile file = new VirtualFileImpl(editedCommand.getName() + ".sh", content);

        editor.init(new EditorInputImpl(fileTypeRegistry.getFileTypeByFile(file), file), new OpenEditorCallbackImpl());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        return !initialValue.equals(getCommandPropertyValue());
    }

    /** Returns the current value of the edited command's property. */
    protected abstract String getCommandPropertyValue();

    /**
     * Updates the value of the edited command's property.
     *
     * @param content
     *         new value modified by user
     */
    protected abstract void updateCommandPropertyValue(String content);

    @Override
    public void onExploreMacros() {
        macroChooser.show(new MacroChosenCallback() {
            @Override
            public void onMacroChosen(Macro macro) {
                final Document document = editor.getDocument();

                document.replace(document.getCursorOffset(), 0, macro.getName());
            }
        });
    }
}
