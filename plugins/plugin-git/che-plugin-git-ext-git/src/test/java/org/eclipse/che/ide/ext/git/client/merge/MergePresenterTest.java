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
package org.eclipse.che.ide.ext.git.client.merge;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.MergeResult.MergeStatus.ALREADY_UP_TO_DATE;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.merge.MergePresenter.MERGE_COMMAND_NAME;

/**
 * Testing {@link MergePresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class MergePresenterTest extends BaseTest {
    public static final String DISPLAY_NAME = "displayName";
    public static final String FILE_PATH    = "/src/testClass.java";

    @Mock
    private MergeView           view;
    @Mock
    private MergeResult         mergeResult;
    @Mock
    private EditorInput         editorInput;
    @Mock
    private EditorAgent         editorAgent;
    @Mock
    private Reference           selectedReference;
    @Mock
    private EditorPartPresenter partPresenter;
    @Mock
    private VirtualFile         file;
    private MergePresenter      presenter;

    @Override
    public void disarm() {
        super.disarm();
        presenter = new MergePresenter(view,
                                       eventBus,
                                       editorAgent,
                                       service,
                                       constant,
                                       appContext,
                                       notificationManager,
                                       dialogFactory,
                                       dtoUnmarshallerFactory,
                                       projectExplorer,
                                       gitOutputConsoleFactory,
                                       consolesPanelPresenter);
        List<EditorPartPresenter> partPresenterList = new ArrayList<>();
        partPresenterList.add(partPresenter);

        when(mergeResult.getMergeStatus()).thenReturn(ALREADY_UP_TO_DATE);
        when(selectedReference.getDisplayName()).thenReturn(DISPLAY_NAME);
        when(editorAgent.getOpenedEditors()).thenReturn(partPresenterList);
        when(partPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(file);
        when(file.getPath()).thenReturn(FILE_PATH);
    }

    @Test
    public void testShowDialogWhenAllOperationsAreSuccessful() throws Exception {
        final List<Branch> branches = new ArrayList<>();
        branches.add(mock(Branch.class));

        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
            onSuccess.invoke(callback, branches);
            return callback;
        }).when(service).branchList(devMachine, anyObject(), eq(LIST_LOCAL), (AsyncRequestCallback<List<Branch>>)anyObject());

        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
            onSuccess.invoke(callback, branches);
            return callback;
        }).when(service).branchList(eq(devMachine), anyObject(), eq(LIST_REMOTE), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(view).setEnableMergeButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
        verify(service).branchList(eq(devMachine), eq(rootProjectConfig), eq(LIST_LOCAL), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(service).branchList(eq(devMachine), eq(rootProjectConfig), eq(LIST_REMOTE), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(view).setRemoteBranches((List<Reference>)anyObject());
        verify(view).setLocalBranches((List<Reference>)anyObject());
        verify(notificationManager, never()).notify(anyString(), rootProjectConfig);
        verify(console, never()).printError(anyString());
    }

    @Test
    public void testShowDialogWhenAllOperationsAreFailed() throws Exception {
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
            onFailure.invoke(callback, mock(Throwable.class));
            return callback;
        }).when(service).branchList(devMachine, anyObject(), eq(LIST_LOCAL), (AsyncRequestCallback<List<Branch>>)anyObject());

        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
            onFailure.invoke(callback, mock(Throwable.class));
            return callback;
        }).when(service).branchList(devMachine, anyObject(), eq(LIST_REMOTE), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(service).branchList(eq(devMachine), eq(rootProjectConfig), eq(LIST_LOCAL), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(service).branchList(eq(devMachine), eq(rootProjectConfig), eq(LIST_REMOTE), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(gitOutputConsoleFactory).create(MERGE_COMMAND_NAME);
        verify(console, times(2)).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager, times(2)).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnMergeClickedWhenMergeRequestIsSuccessful() throws Exception {
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<MergeResult> callback = (AsyncRequestCallback<MergeResult>)arguments[2];
            Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
            onSuccess.invoke(callback, mergeResult);
            return callback;
        }).when(service).merge(devMachine, anyObject(), anyString(), (AsyncRequestCallback<MergeResult>)anyObject());

        presenter.onReferenceSelected(selectedReference);
        presenter.onMergeClicked();

        verify(view).close();
        verify(editorAgent).getOpenedEditors();
        verify(service).merge(eq(devMachine), eq(rootProjectConfig), eq(DISPLAY_NAME), (AsyncRequestCallback<MergeResult>)anyObject());
        verify(appContext).getCurrentProject();
        verify(partPresenter).getEditorInput();
        verify(file).getPath();
        verify(gitOutputConsoleFactory).create(MERGE_COMMAND_NAME);
        verify(console).print(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnMergeClickedWhenMergeRequestIsFailed() throws Exception {
        when(selectedReference.getDisplayName()).thenReturn(DISPLAY_NAME);

        presenter.onReferenceSelected(selectedReference);
        presenter.onMergeClicked();

        verify(service).merge(eq(devMachine), anyObject(), eq(DISPLAY_NAME), (AsyncRequestCallback<MergeResult>)anyObject());
    }

    @Test
    public void testOnReferenceSelected() throws Exception {
        when(selectedReference.getDisplayName()).thenReturn(DISPLAY_NAME);

        presenter.onReferenceSelected(selectedReference);
        presenter.onMergeClicked();

        verify(service).merge(eq(devMachine), anyObject(), eq(DISPLAY_NAME), (AsyncRequestCallback<MergeResult>)anyObject());
    }

    @Test
    public void testDialogWhenListOfBranchesAreEmpty() throws Exception {
        final ArrayList<Reference> emptyReferenceList = new ArrayList<>();
        final List<Branch> emptyBranchList = new ArrayList<>();

        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
            onSuccess.invoke(callback, emptyBranchList);
            return callback;
        }).when(service).branchList(devMachine, anyObject(), eq(LIST_LOCAL), (AsyncRequestCallback<List<Branch>>)anyObject());

        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
            Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
            onSuccess.invoke(callback, emptyBranchList);
            return callback;
        }).when(service).branchList(eq(devMachine), anyObject(), eq(LIST_REMOTE), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(view).showDialog();
        verify(view).setLocalBranches(eq(emptyReferenceList));
        verify(view).setRemoteBranches(eq(emptyReferenceList));
    }
}
