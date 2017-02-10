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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.common.base.Optional;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileStructurePresenterTest {

    @Mock
    private FileStructure         view;
    @Mock
    private JavaNavigationService javaNavigationService;
    @Mock
    private AppContext            context;
    @Mock
    private EditorAgent           editorAgent;
    @Mock
    private MessageLoader         loader;
    @Mock
    private LoaderFactory         loaderFactory;

    @Mock
    private TextEditorPresenter      editorPartPresenter;
    @Mock
    private EditorInput              editorInput;
    @Mock
    private File                        file;
    @Mock
    private Project                     relatedProject;
    @Mock
    private Container                   srcFolder;
    @Mock
    private Promise<CompilationUnit>    promise;
    @Mock
    private Promise<Node>            nodePromise;
    @Mock
    private CompilationUnit          compilationUnit;
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
        when(editorInput.getFile()).thenReturn(file);
        when(editorPartPresenter.getCursorOffset()).thenReturn(0);
        when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
        when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
        when(file.getName()).thenReturn("A.java");
        when(file.getExtension()).thenReturn("java");
        when(file.getResourceType()).thenReturn(Resource.FILE);
        when(file.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/c/A.java"));
        when(srcFolder.getLocation()).thenReturn(Path.valueOf("/project/src"));
        when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));
        when(javaNavigationService.getCompilationUnit(any(Path.class), anyString(), anyBoolean())).thenReturn(promise);
        when(promise.then(Matchers.<Operation<CompilationUnit>>anyObject())).thenReturn(promise);
        when(promise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(promise);
        when(loaderFactory.newLoader()).thenReturn(loader);


        presenter = new FileStructurePresenter(view,
                                               javaNavigationService,
                                               context,
                                               editorAgent,
                                               loaderFactory);
    }

    @Test
    public void fileStructureShouldBeShow() throws Exception {
        presenter.show(editorPartPresenter);

        verify(loader).show();
        verify(view).setTitle("A.java");

        verify(promise).then(operationSuccessCapture.capture());
        operationSuccessCapture.getValue().apply(compilationUnit);

        verify(view).setStructure(compilationUnit, false);
        verify(loader).hide();
    }

    @Test
    public void loaderShouldBeHideIfSomethingIsWrong() throws Exception {
        PromiseError promiseError = Mockito.mock(PromiseError.class);
        presenter.show(editorPartPresenter);

        verify(promise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(promiseError);

        verify(view, never()).setStructure(compilationUnit, false);
        verify(promiseError).getMessage();
        verify(loader).hide();
    }

    @Test
    public void binaryClassShouldBeOpenedIfMemberIsBinary() throws Exception {
        when(member.isBinary()).thenReturn(true);
        when(nodePromise.then(Matchers.<Operation<Node>>anyObject())).thenReturn(nodePromise);

        presenter.show(editorPartPresenter);
    }

    @Test
    public void selectMemberIfItIsNotBinary() throws Exception {
        when(member.isBinary()).thenReturn(false);
        when(nodePromise.then(Matchers.<Function<Node, Node>>anyObject())).thenReturn(nodePromise);

        presenter.show(editorPartPresenter);
    }

    @Test
    public void cursorShouldBeReturnedInPreviousPositionAfterDialogClosingByEscapeButton() {
        presenter.show(editorPartPresenter);

        presenter.onEscapeClicked();

        verify(editorPartPresenter).setFocus();
        verify(editorPartPresenter).getDocument();
        verify(document).setSelectedRange(Matchers.<LinearRange>anyObject(), eq(true));
    }
}
