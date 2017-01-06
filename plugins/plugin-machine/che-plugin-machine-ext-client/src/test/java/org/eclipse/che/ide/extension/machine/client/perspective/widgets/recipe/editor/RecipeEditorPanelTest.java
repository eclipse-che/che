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

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeEditorPanelTest {
    private final String SOME_TEXT = "text";

    //constructor mocks
    @Mock
    private RecipeEditorView               view;
    @Mock
    private RecipeFileFactory              recipeFileFactory;
    @Mock
    private FileTypeRegistry               fileTypeRegistry;
    @Mock
    private RecipeDescriptor               recipeDescriptor;
    @Mock
    private Provider<OrionEditorPresenter> orionTextEditorFactory;

    @Mock
    private VirtualFile                            recipeFile;
    @Mock
    private RecipeEditorPanel.ActionDelegate       delegate;
    @Mock
    private OrionEditorPresenter editor;

    private RecipeEditorPanel presenter;

    @Before
    public void setUp() throws Exception {
        when(recipeDescriptor.getScript()).thenReturn(SOME_TEXT);
        when(recipeFileFactory.newInstance(anyString())).thenReturn(recipeFile);
        when(orionTextEditorFactory.get()).thenReturn(editor);

        presenter = new RecipeEditorPanel(recipeFileFactory,
                                          fileTypeRegistry,
                                          orionTextEditorFactory,
                                          view,
                                          recipeDescriptor);

        presenter.setDelegate(delegate);

    }

    @Test
    public void constructorShouldBePerformed() throws Exception {
        verify(view).setDelegate(presenter);
        verify(view).setEnableSaveButton(false);
        verify(view).setEnableCancelButton(false);
    }

    @Test
    public void saveCancelDeleteButtonsShouldBeEnabled() throws Exception {
        presenter.setEnableSaveCancelCloneDeleteBtns(true);

        verify(view).setEnableCancelButton(true);
        verify(view).setEnableDeleteButton(true);
        verify(view).setEnableSaveButton(true);
    }

    @Test
    public void saveCancelDeleteButtonsShouldBeVisible() throws Exception {
        presenter.setVisibleSaveCancelCloneDeleteBtns(true);

        verify(view).setVisibleDeleteButton(true);
        verify(view).setVisibleCancelButton(true);
        verify(view).setVisibleSaveButton(true);
    }

    @Test
    public void saveCancelDeleteButtonsShouldBeHidden() throws Exception {
        presenter.setVisibleSaveCancelCloneDeleteBtns(false);

        verify(view).setVisibleDeleteButton(false);
        verify(view).setVisibleCancelButton(false);
        verify(view).setVisibleSaveButton(false);
    }

    @Test
    public void tagsShouldBeReturned() throws Exception {
        presenter.getTags();

        verify(view).getTags();
    }

    @Test
    public void editorShouldBeInitializedAndShowed() throws Exception {
        FileType fileType = mock(FileType.class);
        when(fileTypeRegistry.getFileTypeByFile(recipeFile)).thenReturn(fileType);

        presenter.showEditor();

        verify(recipeDescriptor).getScript();
        verify(recipeFileFactory).newInstance(SOME_TEXT);

        verify(orionTextEditorFactory).get();
        verify(editor).activate();
        verify(editor).onOpen();
        verify(view).showEditor(editor);
    }

    @Test
    public void editorShouldBeShowed() throws Exception {
        FileType fileType = mock(FileType.class);
        when(fileTypeRegistry.getFileTypeByFile(recipeFile)).thenReturn(fileType);

        presenter.showEditor();
        presenter.showEditor();

        verify(recipeDescriptor).getScript();
        verify(recipeFileFactory).newInstance(SOME_TEXT);

        verify(orionTextEditorFactory).get();
        verify(editor).activate();
        verify(editor).onOpen();
        verify(view).showEditor(editor);
    }

    @Test
    public void scriptShouldBeReturned() throws Exception {
        FileType fileType = mock(FileType.class);
        Document document = mock(Document.class);
        when(fileTypeRegistry.getFileTypeByFile(recipeFile)).thenReturn(fileType);
        when(editor.getDocument()).thenReturn(document);

        presenter.showEditor();
        presenter.getScript();

        verify(editor).getDocument();
        verify(document).getContents();
    }

    @Test
    public void verifyCreateButtonClick() throws Exception {
        presenter.onCloneButtonClicked();

        verify(delegate).onCloneButtonClicked();
    }

    @Test
    public void verifySaveButtonClick() throws Exception {
        presenter.onSaveButtonClicked();

        verify(view, times(2)).setEnableCancelButton(false);
        verify(view, times(2)).setEnableSaveButton(false);
        verify(delegate).onSaveButtonClicked();
    }

    @Test
    public void verifyDeleteButtonClick() throws Exception {
        presenter.onDeleteButtonClicked();

        verify(delegate).onDeleteButtonClicked();
    }

    @Test
    public void verifyCancelButtonClick() throws Exception {
        List<String> tags = Arrays.asList("tag");
        when(recipeDescriptor.getTags()).thenReturn(tags);

        presenter.onCancelButtonClicked();

        verify(view, times(2)).setEnableCancelButton(false);
        verify(view, times(2)).setEnableSaveButton(false);
        verify(view).setTags(tags);
    }

    @Test
    public void viewShouldBeReturned() throws Exception {
        assertEquals(view, presenter.getView());
    }
}
