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
package org.eclipse.che.ide.ext.git.client.remove;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.remove.RemoveFromIndexPresenter.REMOVE_FROM_INDEX_COMMAND_NAME;

/**
 * Testing {@link RemoveFromIndexPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class RemoveFromIndexPresenterTest extends BaseTest {
    public static final boolean  REMOVED   = true;
    public static final String   MESSAGE   = "message";
    public static final SafeHtml SAFE_HTML = mock(SafeHtml.class);
    @Mock
    private RemoveFromIndexView      view;
    private RemoveFromIndexPresenter presenter;
    @Mock
    private EditorAgent              editorAgent;
    @Mock
    private EditorPartPresenter      partPresenter;
    @Mock
    private EditorInput              editorInput;
    @Mock
    private FileReferenceNode        file;
    @Mock
    private ProjectExplorerPresenter projectExplorer;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new RemoveFromIndexPresenter(view,
                                                 eventBus,
                                                 service,
                                                 constant,
                                                 appContext,
                                                 selectionAgent,
                                                 notificationManager,
                                                 editorAgent,
                                                 projectExplorer,
                                                 gitOutputConsoleFactory,
                                                 consolesPanelPresenter);
        NavigableMap<String, EditorPartPresenter> partPresenterMap = new TreeMap<>();
        partPresenterMap.put("partPresenter", partPresenter);

        when(editorAgent.getOpenedEditors()).thenReturn(partPresenterMap);
        when(partPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(file);
    }

    @Test
    public void testShowDialogWhenSomeFileIsSelected() throws Exception {
        String filePath = PROJECT_PATH + PROJECT_NAME;
        Selection selection = mock(Selection.class);
        FileReferenceNode file = mock(FileReferenceNode.class);
        when(file.getStorablePath()).thenReturn(filePath);
        when(selection.getHeadElement()).thenReturn(file);
        when(selectionAgent.getSelection()).thenReturn(selection);
        when(constant.removeFromIndexFile(anyString())).thenReturn(SAFE_HTML);
        when(SAFE_HTML.asString()).thenReturn(MESSAGE);

        presenter.showDialog();

        verify(view).setMessage(eq(MESSAGE));
        verify(view).setRemoved(eq(!REMOVED));
        verify(view).showDialog();
        verify(constant).removeFromIndexFile(eq(PROJECT_NAME));
    }

    @Test
    public void testShowDialogWhenSomeFolderIsSelected() throws Exception {
        String folderPath = PROJECT_PATH + PROJECT_NAME;
        Selection selection = mock(Selection.class);
        FolderReferenceNode folder = mock(FolderReferenceNode.class);
        when(folder.getStorablePath()).thenReturn(folderPath);
        when(selection.getHeadElement()).thenReturn(folder);
        when(selectionAgent.getSelection()).thenReturn(selection);
        when(constant.removeFromIndexFolder(anyString())).thenReturn(SAFE_HTML);
        when(SAFE_HTML.asString()).thenReturn(MESSAGE);

        presenter.showDialog();

        verify(view).setMessage(eq(MESSAGE));
        verify(view).setRemoved(eq(!REMOVED));
        verify(view).showDialog();
        verify(constant).removeFromIndexFolder(eq(PROJECT_NAME));
    }

    @Test
    public void testShowDialogWhenRootFolderIsSelected() throws Exception {
        Selection selection = mock(Selection.class);
        ProjectNode project = mock(ProjectNode.class);
        when(project.getStorablePath()).thenReturn(PROJECT_PATH);
        when(selection.getHeadElement()).thenReturn(project);
        when(selectionAgent.getSelection()).thenReturn(selection);
        when(constant.removeFromIndexAll()).thenReturn(MESSAGE);

        presenter.showDialog();

        verify(view).setMessage(eq(MESSAGE));
        verify(view).setRemoved(eq(!REMOVED));
        verify(view).showDialog();
        verify(constant).removeFromIndexAll();
    }

    @Test
    public void testOnRemoveClickedWhenRemoveRequestIsSuccessful() throws Exception {
        when(view.isRemoved()).thenReturn(REMOVED);
        when(selectionAgent.getSelection()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service).remove(anyString(), anyObject(), anyObject(), anyBoolean(), anyObject());

        presenter.showDialog();
        presenter.onRemoveClicked();

        verify(service).remove(anyString(), eq(rootProjectConfig), anyObject(), eq(REMOVED), anyObject());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(gitOutputConsoleFactory).create(REMOVE_FROM_INDEX_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(constant, times(2)).removeFilesSuccessfull();
        verify(view).close();
    }

    @Test
    public void testOnRemoveClickedWhenRemoveRequestIsFailed() throws Exception {
        when(view.isRemoved()).thenReturn(REMOVED);
        when(selectionAgent.getSelection()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[3];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).remove(anyString(), anyObject(), anyObject(), anyBoolean(), anyObject());

        presenter.showDialog();
        presenter.onRemoveClicked();

        verify(service).remove(anyString(), eq(rootProjectConfig), anyObject(), eq(REMOVED), anyObject());
        verify(constant).removeFilesFailed();
        verify(gitOutputConsoleFactory).create(REMOVE_FROM_INDEX_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(view).close();
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

}
