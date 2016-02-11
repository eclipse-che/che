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
package org.eclipse.che.ide.jseditor.client.editoradapter;

import org.eclipse.che.ide.api.editor.EditorInitException;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.texteditor.UndoableEditor;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.keymap.Keybinding;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.text.TextRange;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test of the Default editor adapter class.
 *
 * @author Igor Vinokur
 */
@RunWith(GwtMockitoTestRunner.class)
public class DefaultEditorAdapterTest {

    @Mock
    private ConfigurableTextEditor textEditor;

    @Mock
    private NestablePresenter nestedPresenter;

    @Mock
    private EventBus eventBus;

    @Mock
    private WorkspaceAgent workspaceAgent;

    @InjectMocks
    private DefaultEditorAdapter defaultEditorAdapter;

    @Before
    public void prepare() {
        defaultEditorAdapter.setTextEditor(textEditor);
        defaultEditorAdapter.setPresenter(nestedPresenter);
    }

    @Test
    public void shouldClose() {
        defaultEditorAdapter.close(true);
        verify(textEditor).close(true);

        reset(textEditor);

        defaultEditorAdapter.close(false);
        verify(textEditor).close(false);
    }

    @Test
    public void shouldIsEditableCalled() {
        when(textEditor.isEditable()).thenReturn(true);
        assertTrue(defaultEditorAdapter.isEditable());

        when(textEditor.isEditable()).thenReturn(false);
        assertFalse(defaultEditorAdapter.isEditable());
    }

    @Test
    public void shouldRevertToSaved() {
        defaultEditorAdapter.doRevertToSaved();

        verify(textEditor).doRevertToSaved();
    }

    @Test
    public void shouldGetDocument() {
        final Document document = mock(Document.class);

        when(textEditor.getDocument()).thenReturn(document);

        assertEquals(document, defaultEditorAdapter.getDocument());
    }

    @Test
    public void shouldGetContentType() {
        when(textEditor.getContentType()).thenReturn("ContentType");

        assertEquals("ContentType", defaultEditorAdapter.getContentType());
    }

    @Test
    public void shouldGetSelectedTextRange() {
        final TextRange textRange = mock(TextRange.class);

        when(textEditor.getSelectedTextRange()).thenReturn(textRange);

        Assert.assertEquals(textRange, defaultEditorAdapter.getSelectedTextRange());
    }

    @Test
    public void shouldGetSelectedLinearRange() {
        final LinearRange linearRange = mock(LinearRange.class);

        when(textEditor.getSelectedLinearRange()).thenReturn(linearRange);

        assertEquals(linearRange, defaultEditorAdapter.getSelectedLinearRange());
    }

    @Test
    public void shouldGetCursorPosition() {
        final TextPosition textPosition = mock(TextPosition.class);

        when(textEditor.getCursorPosition()).thenReturn(textPosition);

        assertEquals(textPosition, defaultEditorAdapter.getCursorPosition());
    }

    @Test
    public void shouldGetCursorOffset() {
        when(textEditor.getCursorOffset()).thenReturn(15);

        assertEquals(15, defaultEditorAdapter.getCursorOffset());
    }

    @Test
    public void shouldShowMessage() {
        defaultEditorAdapter.showMessage("Message");

        verify(textEditor).showMessage("Message");
    }

    @Test
    public void shouldInit() throws EditorInitException {
        final EditorInput input = mock(EditorInput.class);

        defaultEditorAdapter.init(input);

        verify(textEditor).init(input);
    }

    @Test
    public void shouldGetEditorInput() {
        final EditorInput editorInput = mock(EditorInput.class);

        when(textEditor.getEditorInput()).thenReturn(editorInput);

        assertEquals(editorInput, defaultEditorAdapter.getEditorInput());
    }

    @Test
    public void shouldDoSave() {
        defaultEditorAdapter.doSave();

        verify(textEditor).doSave();
    }

    @Test
    public void shouldDoSaveWithCallback() {
        final AsyncCallback<EditorInput> callback = mock(AsyncCallback.class);

        defaultEditorAdapter.doSave(callback);

        verify(textEditor).doSave(callback);
    }

    @Test
    public void shouldDoSaveAs() {
        defaultEditorAdapter.doSaveAs();

        verify(textEditor).doSaveAs();
    }

    @Test
    public void shouldFileChanged() {
        defaultEditorAdapter.onFileChanged();

        verify(textEditor).onFileChanged();
    }

    @Test
    public void shouldIsDirtyCalled() {
        when(textEditor.isDirty()).thenReturn(true);
        assertTrue(defaultEditorAdapter.isDirty());

        when(textEditor.isDirty()).thenReturn(false);
        assertFalse(defaultEditorAdapter.isDirty());
    }

    @Test
    public void shouldAddCloseHandler() {
        final EditorPartPresenter.EditorPartCloseHandler closeHandler = mock(EditorPartPresenter.EditorPartCloseHandler.class);

        defaultEditorAdapter.addCloseHandler(closeHandler);

        verify(textEditor).addCloseHandler(closeHandler);
    }

    @Test
    public void shouldActivate() {
        defaultEditorAdapter.activate();

        verify(textEditor).activate();
    }

    @Test
    public void shouldGetTitle() {
        when(textEditor.getTitle()).thenReturn("Title");

        assertTrue(defaultEditorAdapter.getTitle().equals("Title"));
    }

    @Test
    public void shouldGetTitleImage() {
        ImageResource imageResource = mock(ImageResource.class);

        when(textEditor.getTitleImage()).thenReturn(imageResource);

        assertEquals(imageResource, defaultEditorAdapter.getTitleImage());
    }

    @Test
    public void shouldGetTitleSVGImage() {
        SVGResource svgResource = mock(SVGResource.class);

        when(textEditor.getTitleSVGImage()).thenReturn(svgResource);

        assertEquals(svgResource, defaultEditorAdapter.getTitleSVGImage());
    }

    @Test
    public void shouldDecorateIcon() {
        SVGImage svgImage = mock(SVGImage.class);
        SVGImage inputSvgImage = mock(SVGImage.class);

        when(textEditor.decorateIcon(inputSvgImage)).thenReturn(svgImage);

        assertEquals(svgImage, defaultEditorAdapter.decorateIcon(inputSvgImage));
    }

    @Test
    public void shouldGetTitleWidget() {
        IsWidget isWidget = mock(IsWidget.class);

        when(textEditor.getTitleWidget()).thenReturn(isWidget);

        assertEquals(isWidget, defaultEditorAdapter.getTitleWidget());
    }

    @Test
    public void shouldGetTitleToolTip() {
        when(textEditor.getTitleToolTip()).thenReturn("TitleToolTip");

        assertEquals("TitleToolTip", defaultEditorAdapter.getTitleToolTip());
    }

    @Test
    public void shouldGetSize() {
        when(textEditor.getSize()).thenReturn(15);

        assertEquals(15, defaultEditorAdapter.getSize());
    }

    @Test
    public void shouldOpen() {
        defaultEditorAdapter.onOpen();

        verify(textEditor).onOpen();
    }

    @Test
    public void shouldCloseWithCallback() {
        final AsyncCallback<Void> callback = mock(AsyncCallback.class);

        defaultEditorAdapter.onClose(callback);

        verify(nestedPresenter).onClose(callback);
    }

    @Test
    public void shouldGetSelection() {
        Selection selection = mock(Selection.class);

        when(textEditor.getSelection()).thenReturn(selection);

        assertEquals(selection, defaultEditorAdapter.getSelection());
    }

    @Test
    public void shouldAddPropertyListener() {
        ArgumentCaptor<PropertyListener> newListener = ArgumentCaptor.forClass(PropertyListener.class);

        final PropertyListener listener = mock(PropertyListener.class);
        final PartPresenter partPresenter = mock(PartPresenter.class);

        defaultEditorAdapter.addPropertyListener(listener);

        verify(textEditor).addPropertyListener(newListener.capture());

        newListener.getAllValues().get(0).propertyChanged(partPresenter, 5);

        verify(listener).propertyChanged(defaultEditorAdapter, 5);
    }

    @Test
    public void shouldGo() {
        final AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        defaultEditorAdapter.go(container);

        verify(nestedPresenter).go(defaultEditorAdapter.panel);
        verify(container).setWidget(defaultEditorAdapter.panel);
    }

    @Test
    public void shouldInitialize() {
        final TextEditorConfiguration configuration = mock(TextEditorConfiguration.class);
        final NotificationManager notificationManager = mock(NotificationManager.class);

        defaultEditorAdapter.initialize(configuration, notificationManager);

        verify(textEditor).initialize(configuration, notificationManager);
    }

    @Test
    public void shouldGetConfiguration() {
        final TextEditorConfiguration editorConfiguration = mock(TextEditorConfiguration.class);

        when(textEditor.getConfiguration()).thenReturn(editorConfiguration);

        assertEquals(editorConfiguration, defaultEditorAdapter.getConfiguration());
    }

    @Test
    public void shouldSetTextEditor() {
        final ConfigurableTextEditor textEditor = mock(ConfigurableTextEditor.class);

        defaultEditorAdapter.setTextEditor(textEditor);

        assertEquals(textEditor, defaultEditorAdapter.getTextEditor());
    }

    @Test
    public void shouldGetTextEditor() {
        assertEquals(textEditor, defaultEditorAdapter.getTextEditor());
    }

    @Test
    public void shouldResize() {
        //Needed to initialise widget of "RequiresResize" instance
        abstract class DummyWidget extends Widget implements RequiresResize {
        }

        DummyWidget widget = mock(DummyWidget.class);

        when(defaultEditorAdapter.panel.getWidget()).thenReturn(widget);

        defaultEditorAdapter.onResize();

        verify(widget).onResize();
    }

    @Test
    public void shouldAddKeybinding() {
        final Keybinding keybinding = mock(Keybinding.class);

        defaultEditorAdapter.addKeybinding(keybinding);

        verify(textEditor).addKeybinding(keybinding);
    }

    @Test
    public void shouldGetUndoRedo() {
        //Needed to initialise textEditor of "UndoableEditor" instance
        abstract class DummyTextEditor implements UndoableEditor, ConfigurableTextEditor {
        }

        ConfigurableTextEditor textEditor = mock(DummyTextEditor.class);

        defaultEditorAdapter.setTextEditor(textEditor);
        defaultEditorAdapter.getUndoRedo();

        verify((UndoableEditor)textEditor).getUndoRedo();

        defaultEditorAdapter.setTextEditor(this.textEditor);

        assertTrue(defaultEditorAdapter.getUndoRedo() instanceof DummyHandlesUndoRedo);
    }

    @Test
    public void shouldCloseEventFiredUp() throws EditorInitException {
        final FileEvent event = mock(FileEvent.class);
        final VirtualFile file = mock(VirtualFile.class);
        final EditorInput input = mock(EditorInput.class);
        String path = "filePath";

        when(input.getFile()).thenReturn(file);
        when(event.getFile()).thenReturn(file);
        when(file.getPath()).thenReturn(path);
        when(event.getOperationType()).thenReturn(FileEvent.FileOperation.CLOSE);

        defaultEditorAdapter.init(input);
        defaultEditorAdapter.onFileOperation(event);

        verify(workspaceAgent).removePart(defaultEditorAdapter);
    }

    @Test
    public void shouldOpenEventFiredUp() throws EditorInitException {
        final FileEvent event = mock(FileEvent.class);

        when(event.getOperationType()).thenReturn(FileEvent.FileOperation.OPEN);

        defaultEditorAdapter.onFileOperation(event);

        verify(workspaceAgent, never()).removePart(defaultEditorAdapter);
    }

    @Test
    public void shouldSaveEventFiredUp() throws EditorInitException {
        final FileEvent event = mock(FileEvent.class);

        when(event.getOperationType()).thenReturn(FileEvent.FileOperation.SAVE);

        defaultEditorAdapter.onFileOperation(event);

        verify(workspaceAgent, never()).removePart(defaultEditorAdapter);
    }

}

