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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class OpenImplementationPresenterTest {
    private final static String TEXT         = "tobe or not to be";
    private final static String PROJECT_PATH = "path";
    private final static String CLASS_NAME   = "Class.java";

    @Mock
    private JavaNavigationService    service;
    @Mock
    private AppContext               context;
    @Mock
    private EditorAgent              editorAgent;
    @Mock
    private ProjectExplorerPresenter projectExplorer;
    @Mock
    private JavaNodeManager          javaNodeManager;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private JavaResources            javaResources;
    @Mock
    private DtoFactory               dtoFactory;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private PopupResources           popupResources;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private JavaLocalizationConstant locale;

    @Mock
    private TextEditorPresenter                   editorPartPresenter;
    @Mock
    private EditorInput                           editorInput;
    @Mock
    private VirtualFile                           virtualFile;
    @Mock
    private Document                              document;
    @Mock
    private PositionConverter                     positionConverter;
    @Mock
    private HasProjectConfig                      hasProjectConfig;
    @Mock
    private ProjectConfigDto                      profileConfig;
    @Mock
    private Promise<ImplementationsDescriptorDTO> promise;
    @Mock
    private ImplementationsDescriptorDTO          implementationsDescriptorDTO;
    @Mock
    private PromiseError                          arg;

    @Captor
    private ArgumentCaptor<Operation<ImplementationsDescriptorDTO>> operationSuccessCapture;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>                 operationErrorCapture;

    private OpenImplementationPresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(editorPartPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorPartPresenter.getDocument()).thenReturn(document);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getName()).thenReturn(CLASS_NAME);
        when(virtualFile.getProject()).thenReturn(hasProjectConfig);
        when(hasProjectConfig.getProjectConfig()).thenReturn(profileConfig);
        when(profileConfig.getPath()).thenReturn(PROJECT_PATH);
        when(service.getImplementations(anyString(), anyString(), anyInt())).thenReturn(promise);
        when(promise.then(Matchers.<Operation<ImplementationsDescriptorDTO>>anyObject())).thenReturn(promise);
        when(promise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(promise);

        presenter = new OpenImplementationPresenter(service,
                                                    context,
                                                    dtoFactory,
                                                    javaResources,
                                                    popupResources,
                                                    locale,
                                                    editorAgent,
                                                    projectExplorer,
                                                    javaNodeManager);
    }

    @Test
    public void implementationsShouldBeShowIfElementHasNotImplementation() throws Exception {
        when(editorPartPresenter.getCursorOffset()).thenReturn(0);

        List<Type> implementations = Collections.emptyList();
        when(implementationsDescriptorDTO.getImplementations()).thenReturn(implementations);

        presenter.show(editorPartPresenter);

        verify(profileConfig).getPath();
        verify(service).getImplementations(PROJECT_PATH, "Class", 0);

        verify(promise).then(operationSuccessCapture.capture());
        operationSuccessCapture.getValue().apply(implementationsDescriptorDTO);
    }

    @Test
    public void hideLoaderAndShowErrorIfSomesingIsWrong() throws Exception {
        when(editorPartPresenter.getCursorOffset()).thenReturn(0);
        when(arg.getMessage()).thenReturn(TEXT);

        presenter.show(editorPartPresenter);

        verify(profileConfig).getPath();
        verify(service).getImplementations(PROJECT_PATH, "Class", 0);

        verify(promise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(arg);

        verify(arg).getMessage();
    }
}