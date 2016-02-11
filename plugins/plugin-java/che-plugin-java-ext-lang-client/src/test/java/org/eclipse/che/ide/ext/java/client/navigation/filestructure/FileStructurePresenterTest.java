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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileStructurePresenterTest {
    private final static String SOME_TEXT    = "text";
    private final static String PROJECT_PATH = "path";
    private final static String CLASS_NAME   = "Class.java";

    @Mock
    private FileStructure            view;
    @Mock
    private JavaNavigationService    javaNavigationService;
    @Mock
    private AppContext               context;
    @Mock
    private EditorAgent              editorAgent;
    @Mock
    private MessageLoader            loader;
    @Mock
    private LoaderFactory            loaderFactory;
    @Mock
    private ProjectExplorerPresenter projectExplorer;
    @Mock
    private JavaNodeManager          javaNodeManager;

    @Mock
    private EmbeddedTextEditorPresenter editorPartPresenter;
    @Mock
    private EditorInput                 editorInput;
    @Mock
    private VirtualFile                 virtualFile;
    @Mock
    private HasProjectConfig            hasProjectConfig;
    @Mock
    private ProjectConfigDto            profileConfig;
    @Mock
    private CurrentProject              currentProject;
    @Mock
    private Promise<CompilationUnit>    promice;
    @Mock
    private Promise<Node>               nodePromise;
    @Mock
    private CompilationUnit             compilationUnit;
    @Mock
    private Member                      member;
    @Mock
    private Node                        node;
    @Mock
    private Region                      region;
    @Mock
    private Document                    document;

    @Captor
    private ArgumentCaptor<Operation<CompilationUnit>>     operationSuccessCapture;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>        operationErrorCapture;
    @Captor
    private ArgumentCaptor<Operation<Node>>                operationNodeCapture;
    @Captor
    private ArgumentCaptor<Function<Node, Node>>           functionNodeCapture;
    @Captor
    private ArgumentCaptor<EditorAgent.OpenEditorCallback> openEditorCallbackArgumentCaptor;


    private FileStructurePresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(editorPartPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorPartPresenter.getDocument()).thenReturn(document);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getName()).thenReturn(CLASS_NAME);
        when(virtualFile.getProject()).thenReturn(hasProjectConfig);
        when(hasProjectConfig.getProjectConfig()).thenReturn(profileConfig);
        when(profileConfig.getPath()).thenReturn(PROJECT_PATH);
        when(javaNavigationService.getCompilationUnit(anyString(), anyString(), anyBoolean())).thenReturn(promice);
        when(promice.then(Matchers.<Operation<CompilationUnit>>anyObject())).thenReturn(promice);
        when(promice.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(promice);
        when(context.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(profileConfig);
        when(member.getLibId()).thenReturn(1);
        when(member.getRootPath()).thenReturn(PROJECT_PATH);
        when(member.getFileRegion()).thenReturn(region);
        when(region.getOffset()).thenReturn(1);
        when(region.getLength()).thenReturn(2);
        when(loaderFactory.newLoader()).thenReturn(loader);


        presenter = new FileStructurePresenter(view,
                                               javaNavigationService,
                                               context,
                                               editorAgent,
                                               loaderFactory,
                                               projectExplorer,
                                               javaNodeManager);
    }

    @Test
    public void fileStructureShouldBeShow() throws Exception {
        presenter.show(editorPartPresenter);

        verify(loader).show();
        verify(view).setTitle(CLASS_NAME);
        verify(profileConfig).getPath();
        verify(javaNavigationService).getCompilationUnit(PROJECT_PATH, "Class", false);

        verify(promice).then(operationSuccessCapture.capture());
        operationSuccessCapture.getValue().apply(compilationUnit);

        verify(view).setStructure(compilationUnit, false);
        verify(loader).hide();
    }

    @Test
    public void loaderShouldBeHideIfSomethingIsWrong() throws Exception {
        PromiseError promiseError = Mockito.mock(PromiseError.class);
        presenter.show(editorPartPresenter);

        verify(promice).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(promiseError);

        verify(view, never()).setStructure(compilationUnit, false);
        verify(promiseError).getMessage();
        verify(loader).hide();
    }

    @Test
    public void binaryClassShouldBeOpenedIfMemberIsBinary() throws Exception {
        when(member.isBinary()).thenReturn(true);
        when(javaNodeManager.getClassNode(profileConfig, 1, PROJECT_PATH)).thenReturn(nodePromise);
        when(nodePromise.then(Matchers.<Operation<Node>>anyObject())).thenReturn(nodePromise);

        presenter.show(editorPartPresenter);
    }

    @Test
    public void selectMemberIfItIsNotBinary() throws Exception {
        when(member.isBinary()).thenReturn(false);
        when(projectExplorer.getNodeByPath(Matchers.anyObject())).thenReturn(nodePromise);
        when(nodePromise.then(Matchers.<Function<Node, Node>>anyObject())).thenReturn(nodePromise);

        presenter.show(editorPartPresenter);
    }
}