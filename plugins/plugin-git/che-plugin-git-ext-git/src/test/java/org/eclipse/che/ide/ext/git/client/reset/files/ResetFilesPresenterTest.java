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
package org.eclipse.che.ide.ext.git.client.reset.files;

import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ResetFilesPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class ResetFilesPresenterTest extends BaseTest {
    @Mock
    private ResetFilesView view;
    @Mock
    private IndexFile      indexFile;

    private ResetFilesPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new ResetFilesPresenter(view,
                                            service,
                                            appContext,
                                            constant,
                                            notificationManager,
                                            dtoFactory,
                                            dialogFactory,
                                            gitOutputConsoleFactory,
                                            processesPanelPresenter);

        when(dtoFactory.createDto(IndexFile.class)).thenReturn(indexFile);
        when(indexFile.withIndexed(anyBoolean())).thenReturn(indexFile);
        when(indexFile.withPath(anyString())).thenReturn(indexFile);
        when(indexFile.getPath()).thenReturn("foo");

        when(service.getStatus(anyObject(), any(Path.class))).thenReturn(statusPromise);
        when(statusPromise.then(any(Operation.class))).thenReturn(statusPromise);
        when(statusPromise.catchError(any(Operation.class))).thenReturn(statusPromise);
    }

    @Test
    public void testShowDialogWhenStatusRequestIsSuccessful() throws Exception {
        final Status status = mock(Status.class);
        List<String> changes = new ArrayList<String>();
        changes.add("Change");
        when(status.getAdded()).thenReturn(changes);
        when(status.getChanged()).thenReturn(changes);
        when(status.getRemoved()).thenReturn(changes);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(status);

        verify(view).setIndexedFiles(anyObject());
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWhenStatusRequestIsSuccessfulButIndexIsEmpty() throws Exception {
        MessageDialog messageDialog = mock(MessageDialog.class);
        when(constant.messagesWarningTitle()).thenReturn("Warning");
        when(constant.indexIsEmpty()).thenReturn("Index is Empty");
        when(dialogFactory.createMessageDialog(anyString(), anyString(), anyObject())).thenReturn(messageDialog);
        final Status status = mock(Status.class);
        List<String> changes = new ArrayList<>();
        when(status.getAdded()).thenReturn(changes);
        when(status.getChanged()).thenReturn(changes);
        when(status.getRemoved()).thenReturn(changes);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(status);

        verify(dialogFactory).createMessageDialog(eq("Warning"), eq("Index is Empty"), anyObject());
        verify(view, never()).setIndexedFiles(anyObject());
        verify(view, never()).showDialog();
    }

    @Test
    public void testShowDialogWhenStatusRequestIsFailed() throws Exception {
        presenter.showDialog(project);

        verify(statusPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);

        verify(console).printError(anyString());
        verify(notificationManager).notify(anyString());
    }

    @Test
    public void testOnResetClickedWhenNothingToReset() throws Exception {
        MessageDialog messageDialog = mock(MessageDialog.class);
        final Status status = mock(Status.class);
        IndexFile indexFile = mock(IndexFile.class);
        when(dtoFactory.createDto(IndexFile.class)).thenReturn(indexFile);
        when(constant.messagesWarningTitle()).thenReturn("Warning");
        when(constant.indexIsEmpty()).thenReturn("Index is Empty");
        when(dialogFactory.createMessageDialog(constant.messagesWarningTitle(), constant.indexIsEmpty(), null)).thenReturn(messageDialog);
        when(indexFile.isIndexed()).thenReturn(true);
        when(indexFile.withIndexed(anyBoolean())).thenReturn(indexFile);
        when(indexFile.withPath(anyString())).thenReturn(indexFile);
        List<String> changes = new ArrayList<String>();
        changes.add("Change");
        when(status.getAdded()).thenReturn(changes);
        when(status.getChanged()).thenReturn(changes);
        when(status.getRemoved()).thenReturn(changes);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(status);

        presenter.onResetClicked();

        verify(view).close();
        verify(console).print(anyString());
        verify(constant, times(2)).nothingToReset();
    }

    @Test
    public void testOnResetClickedWhenResetRequestIsSuccessful() throws Exception {
        final Status status = mock(Status.class);
        List<String> changes = new ArrayList<String>();
        changes.add("Change");
        when(status.getAdded()).thenReturn(changes);
        when(status.getChanged()).thenReturn(changes);
        when(status.getRemoved()).thenReturn(changes);

        when(service.reset(anyObject(), any(Path.class), anyString(), any(ResetRequest.ResetType.class), anyObject())).thenReturn(voidPromise);
        when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
        when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(status);

        presenter.onResetClicked();

        verify(voidPromise).then(voidPromiseCaptor.capture());
        voidPromiseCaptor.getValue().apply(null);

        verify(view).close();
        verify(notificationManager).notify(anyString());
        verify(console).print(anyString());
        verify(constant, times(2)).resetFilesSuccessfully();
    }

    @Test
    public void testOnResetClickedWhenResetRequestIsFailed() throws Exception {
        final Status status = mock(Status.class);
        List<String> changes = new ArrayList<String>();
        changes.add("Change");
        when(status.getAdded()).thenReturn(changes);
        when(status.getChanged()).thenReturn(changes);
        when(status.getRemoved()).thenReturn(changes);

        when(service.reset(anyObject(), any(Path.class), anyString(), any(ResetRequest.ResetType.class), anyObject())).thenReturn(voidPromise);
        when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
        when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(status);

        presenter.onResetClicked();

        verify(voidPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);

        verify(console).printError(anyString());
        verify(notificationManager).notify(anyString());
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }
}
