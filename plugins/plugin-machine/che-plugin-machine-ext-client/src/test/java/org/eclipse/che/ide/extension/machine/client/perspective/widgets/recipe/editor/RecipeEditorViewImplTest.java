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

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.List;

import static org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl.Background.BLUE;
import static org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl.Background.GREY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeEditorViewImplTest {
    private static final String CREATE = "CREATE";
    private static final String CLONE = "CLONE";
    private static final String SAVE   = "SAVE";
    private static final String DELETE = "DELETE";
    private static final String CANCEL = "CANCEL";

    @Mock
    private MachineResources            resources;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private WidgetsFactory              widgetFactory;

    @Mock
    private EditorButtonWidget              newButtonWidget;
    @Mock
    private EditorButtonWidget              cloneButtonWidget;
    @Mock
    private EditorButtonWidget              saveButtonWidget;
    @Mock
    private EditorButtonWidget              cancelButtonWidget;
    @Mock
    private EditorButtonWidget              deleteButtonWidget;
    @Mock
    private RecipeEditorView.ActionDelegate delegate;
    @Mock
    private EditorPartPresenter             editor;

    @Captor
    private ArgumentCaptor<EditorButtonWidget.ActionDelegate> captor;

    private RecipeEditorViewImpl view;

    @Before
    public void setUp() throws Exception {
        when(locale.editorButtonNew()).thenReturn(CREATE);
        when(locale.editorButtonClone()).thenReturn(CLONE);
        when(locale.editorButtonSave()).thenReturn(SAVE);
        when(locale.editorButtonDelete()).thenReturn(DELETE);
        when(locale.editorButtonCancel()).thenReturn(CANCEL);

        when(widgetFactory.createEditorButton(CREATE, BLUE)).thenReturn(newButtonWidget);
        when(widgetFactory.createEditorButton(CLONE, GREY)).thenReturn(cloneButtonWidget);
        when(widgetFactory.createEditorButton(SAVE, GREY)).thenReturn(saveButtonWidget);
        when(widgetFactory.createEditorButton(DELETE, GREY)).thenReturn(deleteButtonWidget);
        when(widgetFactory.createEditorButton(CANCEL, GREY)).thenReturn(cancelButtonWidget);

        view = new RecipeEditorViewImpl(resources, locale, widgetFactory);
        view.setDelegate(delegate);
    }

    @Test
    public void buttonsShouldBeAdded() throws Exception {
        EditorButtonWidget.ActionDelegate createDelegate = buttonShouldBeCreated(CREATE, BLUE, newButtonWidget, newButtonWidget);
        createDelegate.onButtonClicked();
        verify(delegate).onNewButtonClicked();

        EditorButtonWidget.ActionDelegate cloneDelegate = buttonShouldBeCreated(CLONE, GREY, cloneButtonWidget, cloneButtonWidget);
        cloneDelegate.onButtonClicked();
        verify(delegate).onCloneButtonClicked();

        EditorButtonWidget.ActionDelegate saveDelegate = buttonShouldBeCreated(SAVE, GREY, saveButtonWidget, saveButtonWidget);
        saveDelegate.onButtonClicked();
        verify(delegate).onSaveButtonClicked();

        EditorButtonWidget.ActionDelegate deleteDelegate = buttonShouldBeCreated(CANCEL, GREY, cancelButtonWidget, cancelButtonWidget);
        deleteDelegate.onButtonClicked();
        verify(delegate).onCancelButtonClicked();

        EditorButtonWidget.ActionDelegate cancelDelegate = buttonShouldBeCreated(DELETE, GREY, deleteButtonWidget, deleteButtonWidget);
        cancelDelegate.onButtonClicked();
        verify(delegate).onCancelButtonClicked();
    }

    private EditorButtonWidget.ActionDelegate buttonShouldBeCreated(String title,
                                                                    EditorButtonWidgetImpl.Background background,
                                                                    EditorButtonWidget button,
                                                                    EditorButtonWidget buttonWidget) {
        verify(widgetFactory).createEditorButton(title, background);
        verify(buttonWidget).setDelegate(captor.capture());
        verify(view.buttonsPanel).add(button);

        return captor.getValue();
    }

    @Test
    public void buttonSaveShouldBeEnable() {
        view.setEnableSaveButton(true);

        verify(saveButtonWidget).setEnable(true);
    }

    @Test
    public void buttonSaveShouldNotBeEnable() {
        view.setEnableSaveButton(false);

        verify(saveButtonWidget).setEnable(false);
    }

    @Test
    public void buttonCancelShouldBeEnable() {
        view.setEnableCancelButton(true);

        verify(cancelButtonWidget).setEnable(true);
    }

    @Test
    public void buttonCancelShouldNotBeEnable() {
        view.setEnableCancelButton(false);

        verify(cancelButtonWidget).setEnable(false);
    }

    @Test
    public void buttonDeleteShouldBeEnable() {
        view.setEnableDeleteButton(true);

        verify(deleteButtonWidget).setEnable(true);
    }

    @Test
    public void buttonDeleteShouldNotBeEnable() {
        view.setEnableDeleteButton(false);

        verify(deleteButtonWidget).setEnable(false);
    }

    @Test
    public void buttonCloneShouldBeEnable() {
        view.setEnableCloneButton(true);

        verify(cloneButtonWidget).setEnable(true);
    }

    @Test
    public void buttonCloneShouldNotBeEnable() {
        view.setEnableCloneButton(false);

        verify(cloneButtonWidget).setEnable(false);
    }

    @Test
    public void buttonSaveShouldBeVisible() {
        view.setVisibleSaveButton(true);

        verify(saveButtonWidget).setVisible(true);
    }

    @Test
    public void buttonSaveShouldNotBeVisible() {
        view.setVisibleSaveButton(false);

        verify(saveButtonWidget).setVisible(false);
    }

    @Test
    public void buttonDeleteShouldBeVisible() {
        view.setVisibleSaveButton(true);

        verify(saveButtonWidget).setVisible(true);
    }

    @Test
    public void buttonDeleteShouldNotBeVisible() {
        view.setVisibleDeleteButton(false);

        verify(deleteButtonWidget).setVisible(false);
    }

    @Test
    public void buttonCancelShouldBeVisible() {
        view.setVisibleCancelButton(true);

        verify(cancelButtonWidget).setVisible(true);
    }

    @Test
    public void buttonCancelShouldNotBeVisible() {
        view.setVisibleCancelButton(false);

        verify(cancelButtonWidget).setVisible(false);
    }

    @Test
    public void scriptUrlShouldBeReturned() throws Exception {
        view.getScriptUrl();

        verify(view.scriptUrl).getText();
    }

    @Test
    public void editorShouldBeShowed() throws Exception {
        view.showEditor(editor);

        verify(editor).go(view.editorPanel);
    }

    @Test
    public void saveAndCancelButtonsShouldBeEnabledIfTagsAreChanging() throws Exception {
        KeyUpEvent keyUpEvent = mock(KeyUpEvent.class);

        view.onTextInputted(keyUpEvent);

        verify(cancelButtonWidget).setEnable(true);
        verify(saveButtonWidget).setEnable(true);
    }

    @Test
    public void buttonsPanelShouldBeHided() throws Exception {
        view.hideButtonsPanel();

        verify(view.recipePanel).setWidgetHidden(view.buttonsPanel, true);
    }

    @Test
    public void tagsShouldBeReturned1() throws Exception {
        String tags = "t1 t2 t3";
        when(view.tags.getText()).thenReturn(tags);

        List<String> actualTags = view.getTags();

        assertTrue(actualTags.contains("t1"));
        assertTrue(actualTags.contains("t2"));
        assertTrue(actualTags.contains("t3"));
        assertEquals(3, actualTags.size());
    }

    @Test
    public void tagsShouldBeReturned2() throws Exception {
        String tags = " ";
        when(view.tags.getText()).thenReturn(tags);

        List<String> actualTags = view.getTags();

        assertTrue(actualTags.isEmpty());
    }

    @Test
    public void tagsShouldBeReturned3() throws Exception {
        String tags = "t1";
        when(view.tags.getText()).thenReturn(tags);

        List<String> actualTags = view.getTags();

        assertTrue(actualTags.contains("t1"));
        assertEquals(1, actualTags.size());
    }

}