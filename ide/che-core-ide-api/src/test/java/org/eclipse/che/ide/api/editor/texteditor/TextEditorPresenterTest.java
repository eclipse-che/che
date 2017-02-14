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
package org.eclipse.che.ide.api.editor.texteditor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInitException;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget.WidgetInitializedCallback;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * @author Andrienko Alexander
 */
@RunWith(GwtMockitoTestRunner.class)
public class TextEditorPresenterTest {

    @Mock
    private DocumentStorage                   documentStorage;
    @Mock
    private EditorLocalizationConstants       constant;
    @Mock
    private EditorWidgetFactory<EditorWidget> editorWidgetFactory;
    @Mock
    private EditorModule        editorModule;
    @Mock
    private TextEditorPartView                editorView;
    @Mock
    private EventBus                          eventBus;
    @Mock
    private QuickAssistantFactory             quickAssistantFactory;
    @Mock
    private EditorInput                       editorInput;
    @Mock
    private EditorWidget                      editorWidget;
    @Mock
    private Document                          document;
    @Mock
    private TextEditorConfiguration           configuration;
    @Mock
    private LoaderFactory                     loaderFactory;
    @Mock
    private MessageLoader                     loader;

    @Mock
    private ContentFormatter                 contentFormatter;
    @Mock
    private Map<String, CodeAssistProcessor> codeAssistProcessors;

    @InjectMocks
    private TextEditorPresenter<EditorWidget> textEditorPresenter;

    @Test
    public void activateEditorIfEditorWidgetNotNull() throws EditorInitException {
        initializeAndInitEditor();
        textEditorPresenter.activate();

        verify(editorWidget).refresh();
        verify(editorWidget).setFocus();
    }

    @Test
    public void activateEditorIfEditorWidgetNull() throws Exception {
        reset(editorView, eventBus);

        textEditorPresenter.activate();

        Field delayedFocus = TextEditorPresenter.class.getDeclaredField("delayedFocus");
        delayedFocus.setAccessible(true);
        boolean fieldValue = (boolean)delayedFocus.get(textEditorPresenter);

        assertTrue(fieldValue);
    }

    @Test
    public void shouldFormatOperation() throws EditorInitException {
        doReturn(contentFormatter).when(configuration).getContentFormatter();
        initializeAndInitEditor();

        textEditorPresenter.doOperation(TextEditorOperations.FORMAT);

        verify(contentFormatter).format(document);
    }

    @Test
    public void shouldFormatOperationWhenDocumentAndFormatterAreNull() throws EditorInitException {
        textEditorPresenter.initialize(configuration);
        textEditorPresenter.doOperation(TextEditorOperations.FORMAT);

        verify(contentFormatter, never()).format(document);
    }

    @Test
    public void shouldFormatOperationWhenFormatterIsNotNullButDocumentIsNull() throws EditorInitException {
        doReturn(contentFormatter).when(configuration).getContentFormatter();

        textEditorPresenter.initialize(configuration);
        textEditorPresenter.doOperation(TextEditorOperations.FORMAT);

        verify(contentFormatter, never()).format(document);
    }

    @Test
    public void shouldFormatOperationWhenDocumentIsNotNullButFormatterIsNull() throws EditorInitException {
        doReturn(null).when(configuration).getContentFormatter();
        initializeAndInitEditor();

        textEditorPresenter.doOperation(TextEditorOperations.FORMAT);

        verify(contentFormatter, never()).format(document);
    }

    @Test
    public void shouldCanDoOperationCodeAssistProposal() throws EditorInitException {
        doReturn(codeAssistProcessors).when(configuration).getContentAssistantProcessors();
        doReturn(false).when(codeAssistProcessors).isEmpty();
        initializeAndInitEditor();

        assertTrue(textEditorPresenter.canDoOperation(TextEditorOperations.CODEASSIST_PROPOSALS));
    }

    @Test
    public void shouldNOtCanDoOperationCodeAssistProposalBecauseProcessorsDontExistInMap() throws EditorInitException {
        doReturn(codeAssistProcessors).when(configuration).getContentAssistantProcessors();
        doReturn(true).when(codeAssistProcessors).isEmpty();
        initializeAndInitEditor();

        assertFalse(textEditorPresenter.canDoOperation(TextEditorOperations.CODEASSIST_PROPOSALS));
    }

    @Test
    public void shouldNOtCanDoOperationCodeAssistProposalBecauseMapOfProcessorsIsNull() throws EditorInitException {
        doReturn(null).when(configuration).getContentAssistantProcessors();
        initializeAndInitEditor();

        assertFalse(textEditorPresenter.canDoOperation(TextEditorOperations.CODEASSIST_PROPOSALS));
    }

    @Test
    public void shouldCanDoOperationFormat() throws EditorInitException {
        doReturn(contentFormatter).when(configuration).getContentFormatter();
        initializeAndInitEditor();

        assertTrue(textEditorPresenter.canDoOperation(TextEditorOperations.FORMAT));
    }

    @Test
    public void shouldNotCanDoOperationFormat() throws EditorInitException {
        doReturn(null).when(configuration).getContentFormatter();
        initializeAndInitEditor();

        assertFalse(textEditorPresenter.canDoOperation(TextEditorOperations.FORMAT));
    }

    /**
     * This method initialize TextEditorPresenter for testing
     * @throws EditorInitException
     */
    public void initializeAndInitEditor() throws EditorInitException {
        reset(Scheduler.get());
        ArgumentCaptor<EditorInitCallback> callBackCaptor = ArgumentCaptor.forClass(EditorInitCallback.class);
        ArgumentCaptor<WidgetInitializedCallback> widgetInitializedCallbackCaptor =
                ArgumentCaptor.forClass(WidgetInitializedCallback.class);
        final EditorAgent.OpenEditorCallback editorCallback = mock(EditorAgent.OpenEditorCallback.class);

        doReturn(loader).when(loaderFactory).newLoader();
        doReturn(editorWidget).when(editorWidgetFactory).createEditorWidget(Matchers.<List<String>>anyObject(),
                                                                            Matchers.<WidgetInitializedCallback>anyObject());
        doReturn(document).when(editorWidget).getDocument();

        textEditorPresenter.injectAsyncLoader(loaderFactory);
        textEditorPresenter.initialize(configuration);
        textEditorPresenter.init(editorInput, editorCallback);

        verify(documentStorage).getDocument(any(VirtualFile.class), callBackCaptor.capture());

        EditorInitCallback editorInitCallBack = callBackCaptor.getValue();
        editorInitCallBack.onReady("test");

        verify(editorWidgetFactory).createEditorWidget(anyListOf(String.class), widgetInitializedCallbackCaptor.capture());
        WidgetInitializedCallback callback = widgetInitializedCallbackCaptor.getValue();
        callback.initialized(editorWidget);
    }
}
