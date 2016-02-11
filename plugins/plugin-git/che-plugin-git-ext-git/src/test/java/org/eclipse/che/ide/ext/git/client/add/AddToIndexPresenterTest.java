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
package org.eclipse.che.ide.ext.git.client.add;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.add.AddToIndexPresenter.ADD_TO_INDEX_COMMAND_NAME;

/**
 * Testing {@link AddToIndexPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class AddToIndexPresenterTest extends BaseTest {
    public static final boolean  NEED_UPDATING = true;
    public static final SafeHtml SAFE_HTML     = mock(SafeHtml.class);
    public static final String   MESSAGE       = "message";

    @Captor
    private ArgumentCaptor<RequestCallback<Void>>        requestCallbackAddToIndexCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<Status>> asyncRequestCallbackStatusCaptor;

    @Mock
    private AddToIndexView           view;
    @Mock
    private ProjectExplorerPresenter projectExplorer;
    @Mock
    private Status                   statusResponse;

    private AddToIndexPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();
        when(appContext.getWorkspaceId()).thenReturn("id");
        presenter = new AddToIndexPresenter(view,
                                            appContext,
                                            dtoUnmarshallerFactory,
                                            constant,
                                            gitOutputConsoleFactory,
                                            consolesPanelPresenter,
                                            service,
                                            notificationManager,
                                            projectExplorer);
    }

    @Test
    public void testDialogWillNotBeShownWhenStatusRequestIsFailed() throws Exception {
        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
        onFailure.invoke(callback, mock(Throwable.class));

        verify(gitOutputConsoleFactory).create(ADD_TO_INDEX_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), anyObject(), eq(true), eq(rootProjectConfig));
        verify(view, never()).showDialog();
        verify(constant, times(2)).statusFailed();
    }

    @Test
    public void testDialogWillNotBeShownWhenNothingAddToIndex() throws Exception {
        when(this.statusResponse.isClean()).thenReturn(true);

        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, this.statusResponse);

        verify(gitOutputConsoleFactory).create(ADD_TO_INDEX_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), eq(rootProjectConfig));
        verify(view, never()).showDialog();
        verify(constant, times(2)).nothingAddToIndex();
    }

    @Test
    public void testShowDialogWhenRootFolderIsSelected() throws Exception {
        Selection selection = mock(Selection.class);
        ProjectNode project = mock(ProjectNode.class);
        when(project.getStorablePath()).thenReturn(PROJECT_PATH);
        when(selection.getHeadElement()).thenReturn(project);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.isSingleSelection()).thenReturn(true);
        when(projectExplorer.getSelection()).thenReturn(selection);
        when(constant.addToIndexAllChanges()).thenReturn(MESSAGE);
        when(this.statusResponse.isClean()).thenReturn(false);

        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, this.statusResponse);

        verify(appContext).getCurrentProject();
        verify(constant).addToIndexAllChanges();
        verify(view).setMessage(eq(MESSAGE), Matchers.<List<String>>eq(null));
        verify(view).setUpdated(anyBoolean());
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWhenSomeFolderIsSelected() throws Exception {
        String folderPath = PROJECT_PATH + PROJECT_NAME;
        Selection selection = mock(Selection.class);
        FolderReferenceNode folder = mock(FolderReferenceNode.class);
        when(folder.getStorablePath()).thenReturn(folderPath);
        when(selection.getHeadElement()).thenReturn(folder);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.isSingleSelection()).thenReturn(true);
        when(projectExplorer.getSelection()).thenReturn(selection);
        when(constant.addToIndexFolder(anyString())).thenReturn(SAFE_HTML);
        when(this.statusResponse.isClean()).thenReturn(false);

        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, this.statusResponse);

        verify(appContext).getCurrentProject();
        verify(constant).addToIndexFolder(eq(PROJECT_NAME));
        verify(view).setUpdated(anyBoolean());
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWhenSomeFileIsSelected() throws Exception {
        String filePath = PROJECT_PATH + PROJECT_NAME;
        Selection selection = mock(Selection.class);
        FileReferenceNode file = mock(FileReferenceNode.class);
        when(file.getPath()).thenReturn(filePath);
        when(selection.getHeadElement()).thenReturn(file);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.isSingleSelection()).thenReturn(true);
        when(file.getStorablePath()).thenReturn(filePath);
        when(selection.getHeadElement()).thenReturn(file);
        when(projectExplorer.getSelection()).thenReturn(selection);
        when(constant.addToIndexFile(anyString())).thenReturn(SAFE_HTML);
        when(SAFE_HTML.asString()).thenReturn(MESSAGE);
        when(this.statusResponse.isClean()).thenReturn(false);

        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, this.statusResponse);

        verify(appContext).getCurrentProject();
        verify(constant).addToIndexFile(eq(PROJECT_NAME));
        verify(view).setMessage(eq(MESSAGE), Matchers.<List<String>>eq(null));
        verify(view).setUpdated(anyBoolean());
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogTwoFileAreSelected() throws Exception {
        final Selection selection = mock(Selection.class);
        // first file
        final String filePath = PROJECT_PATH + PROJECT_NAME;
        final FileReferenceNode file1 = mock(FileReferenceNode.class);
        when(file1.getPath()).thenReturn(filePath);
        when(file1.getStorablePath()).thenReturn(filePath);

        //second file
        final String file2Path = PROJECT_PATH + "test2";
        final FileReferenceNode file2 = mock(FileReferenceNode.class);
        when(file2.getPath()).thenReturn(file2Path);
        when(file2.getStorablePath()).thenReturn(file2Path);

        final List<HasStorablePath> files = new ArrayList<HasStorablePath>() {{
            add(file1);
            add(file2);
        }};
        when(selection.getAllElements()).thenReturn(files);
        when(selection.getHeadElement()).thenReturn(file1);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.isSingleSelection()).thenReturn(false);

        when(projectExplorer.getSelection()).thenReturn(selection);
        when(constant.addToIndexMultiple()).thenReturn(MESSAGE);
        when(this.statusResponse.isClean()).thenReturn(false);

        presenter.showDialog();

        verify(service).status(anyString(), eq(rootProjectConfig), asyncRequestCallbackStatusCaptor.capture());
        final AsyncRequestCallback<Status> callback = asyncRequestCallbackStatusCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        final Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, this.statusResponse);

        verify(appContext).getCurrentProject();
        verify(constant).addToIndexMultiple();
        verify(view).setMessage(eq(MESSAGE), Matchers.<List<String>>anyObject());
        verify(view).setUpdated(anyBoolean());
        verify(view).showDialog();
    }

    @Test
    public void testOnAddClickedWhenAddWSRequestIsSuccessful() throws Exception {
        reset(gitOutputConsoleFactory);
        when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);
        when(view.isUpdated()).thenReturn(NEED_UPDATING);
        when(constant.addSuccess()).thenReturn(MESSAGE);

        presenter.showDialog();
        presenter.onAddClicked();

        verify(service)
                .add(anyString(), eq(rootProjectConfig), eq(NEED_UPDATING), (List<String>)anyObject(), requestCallbackAddToIndexCaptor.capture());
        RequestCallback<Void> callback = requestCallbackAddToIndexCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, (Void)null);

        verify(view).isUpdated();
        verify(view).close();
        verify(service).add(anyString(), eq(rootProjectConfig), eq(NEED_UPDATING), (List<String>)anyObject(),
                            (RequestCallback<Void>)anyObject());
        verify(gitOutputConsoleFactory, times(2)).create(ADD_TO_INDEX_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), eq(rootProjectConfig));
        verify(constant, times(2)).addSuccess();
    }

    @Test
    public void testOnAddClickedWhenAddWSRequestIsFailed() throws Exception {
        reset(gitOutputConsoleFactory);
        when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);
        when(view.isUpdated()).thenReturn(NEED_UPDATING);

        presenter.showDialog();
        presenter.onAddClicked();

        verify(service)
                .add(anyString(), eq(rootProjectConfig), eq(NEED_UPDATING), (List<String>)anyObject(), requestCallbackAddToIndexCaptor.capture());
        RequestCallback<Void> callback = requestCallbackAddToIndexCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
        onFailure.invoke(callback, mock(Throwable.class));

        verify(view).isUpdated();
        verify(view).close();
        verify(gitOutputConsoleFactory, times(2)).create(ADD_TO_INDEX_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), anyObject(), eq(true), eq(rootProjectConfig));
        verify(constant, times(2)).addFailed();
    }

    @Test
    public void testOnAddClickedWhenAddRequestIsFailed() throws Exception {
        reset(gitOutputConsoleFactory);
        when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);
        doThrow(WebSocketException.class).when(service)
                                         .add(anyString(), anyObject(), anyBoolean(), anyObject(), anyObject());
        when(view.isUpdated()).thenReturn(NEED_UPDATING);

        presenter.showDialog();
        presenter.onAddClicked();

        verify(view).isUpdated();
        verify(service).add(anyString(), eq(rootProjectConfig), eq(NEED_UPDATING), anyObject(), anyObject());
        verify(view).close();
        verify(gitOutputConsoleFactory, times(2)).create(ADD_TO_INDEX_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), anyObject(), eq(true), eq(rootProjectConfig));
        verify(constant, times(2)).addFailed();
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }
}
