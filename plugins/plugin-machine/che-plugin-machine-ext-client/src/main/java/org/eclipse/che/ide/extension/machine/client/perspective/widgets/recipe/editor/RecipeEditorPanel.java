/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_DIRTY;
import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_INPUT;

/**
 * The class that manages Properties panel widget.
 *
 * @author Valeriy Svydenko
 */
public class RecipeEditorPanel implements TabPresenter, RecipeEditorView.ActionDelegate {
    private final RecipeEditorView               view;
    private final RecipeFileFactory              recipeFileFactory;
    private final FileTypeRegistry               fileTypeRegistry;
    private final RecipeDescriptor               recipeDescriptor;
    private final Provider<OrionEditorPresenter> orionTextEditorFactory;

    private EditorPartPresenter editor;
    private ActionDelegate      delegate;
    private int                 undoOperations;
    private boolean             isInitialized;


    @AssistedInject
    public RecipeEditorPanel(RecipeFileFactory recipeFileFactory,
                             FileTypeRegistry fileTypeRegistry,
                             Provider<OrionEditorPresenter> orionTextEditorFactory,
                             RecipeEditorView view,
                             @Assisted @NotNull RecipeDescriptor recipeDescriptor) {
        this.view = view;
        this.recipeFileFactory = recipeFileFactory;
        this.orionTextEditorFactory = orionTextEditorFactory;
        this.fileTypeRegistry = fileTypeRegistry;
        this.recipeDescriptor = recipeDescriptor;
        this.isInitialized = false;
        this.view.setDelegate(this);

        setEnableSaveAndCancelButtons(false);
    }

    /** Sets the delegate to receive events from this view. */
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** Changes enable state of 'Cancel', 'Save' and 'Delete' buttons. */
    public void setEnableSaveCancelCloneDeleteBtns(boolean enable) {
        view.setEnableCancelButton(enable);
        view.setEnableSaveButton(enable);
        view.setEnableDeleteButton(enable);
        view.setEnableCloneButton(enable);
    }

    public void setVisibleSaveCancelCloneDeleteBtns(boolean visible) {
        view.setVisibleCancelButton(visible);
        view.setVisibleDeleteButton(visible);
        view.setVisibleSaveButton(visible);
        view.setVisibleCloneButton(visible);
    }

    /** Returns a script of recipe */
    @NotNull
    public String getScript() {
        return ((TextEditor)editor).getDocument().getContents();
    }

    /** Returns list of tags. */
    @NotNull
    public List<String> getTags() {
        return view.getTags();
    }

    /** Returns name of the recipe. */
    @NotNull
    public String getName() {
        return view.getName();
    }

    /** Sets name of the recipe. */
    public void setName(@NotNull String name) {
        view.setName(name);
    }

    /** Sets list of tags. */
    public void setTags(@NotNull List<String> tags) {
        view.setTags(tags);
    }

    /** {@inheritDoc} */
    @Override
    public void showEditor() {
        if (isInitialized) {
            return;
        }
        VirtualFile recipeFile = recipeFileFactory.newInstance(recipeDescriptor.getScript());
        initializeEditor(recipeFile);
        isInitialized = true;
    }

    private void setEnableSaveAndCancelButtons(boolean isEnable) {
        view.setEnableSaveButton(isEnable);
        view.setEnableCancelButton(isEnable);
    }

    private void initializeEditor(@NotNull final VirtualFile file) {
        FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
        editor = getEditor();
        editor.activate();
        editor.onOpen();
        view.showEditor(editor);

        // wait when editor is initialized
        editor.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                switch (propId) {
                    case PROP_INPUT:
                        view.showEditor(editor);
                        break;
                    case PROP_DIRTY:
                        if (validateUndoOperation()) {
                            setEnableSaveAndCancelButtons(true);
                        }
                        break;
                    default:
                }
            }
        });

        editor.init(new RecipeEditorInput(fileType, file), new OpenEditorCallbackImpl());
    }

    private TextEditor getEditor() {
        OrionEditorPresenter editor = orionTextEditorFactory.get();
        editor.initialize(new DefaultTextEditorConfiguration());

        return editor;
    }

    private boolean validateUndoOperation() {
        // this code needs for right behaviour when someone is clicking on 'Cancel' button. We need to make disable some buttons.
        if (undoOperations == 0) {
            return true;
        }

        undoOperations--;
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onCloneButtonClicked() {
        delegate.onCloneButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onNewButtonClicked() {
        delegate.onNewButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onSaveButtonClicked() {
        setEnableSaveAndCancelButtons(false);
        delegate.onSaveButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteButtonClicked() {
        delegate.onDeleteButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        setEnableSaveAndCancelButtons(false);

        view.setTags(recipeDescriptor.getTags());
        view.setName(recipeDescriptor.getName());

        if (editor instanceof UndoableEditor) {
            HandlesUndoRedo undoRedo = ((UndoableEditor)editor).getUndoRedo();
            while (editor.isDirty() && undoRedo.undoable()) {
                undoOperations++;
                undoRedo.undo();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
    }

    public interface ActionDelegate {
        /** Performs some actions in response to user's clicking on the 'Delete' button. */
        void onDeleteButtonClicked();

        /** Performs some actions in response to user's clicking on the 'Create' button. */
        void onCloneButtonClicked();

        /** Performs some actions in response to user's clicking on the 'Create' button. */
        void onNewButtonClicked();

        /** Performs some actions in response to user's clicking on the 'Save' button. */
        void onSaveButtonClicked();

        /** Selects firs created recipe. */
        void selectRecipe();
    }
}
