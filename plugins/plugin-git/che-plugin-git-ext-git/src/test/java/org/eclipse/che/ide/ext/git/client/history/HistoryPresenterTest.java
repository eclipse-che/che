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
package org.eclipse.che.ide.ext.git.client.history;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.DiffRequest.DiffType.RAW;
import static org.eclipse.che.ide.ext.git.client.history.HistoryPresenter.LOG_COMMAND_NAME;
import static org.eclipse.che.ide.ext.git.client.history.HistoryPresenter.DIFF_COMMAND_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link HistoryPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class HistoryPresenterTest extends BaseTest {
    public static final boolean TEXT_NOT_FORMATTED = false;
    public static final String  REVISION_ID        = "revisionId";
    public static final boolean NO_RENAMES         = false;
    public static final int     RENAME_LIMIT       = 0;
    @Mock
    private HistoryView              view;
    @Mock
    private WorkspaceAgent           workspaceAgent;
    @Mock
    private Revision                 selectedRevision;
    @Mock
    private SelectionAgent           selectionAgent;
    @Mock
    private PartStack                partStack;
    @Mock
    private PartPresenter            activePart;
    @Mock
    private LogResponse              logResponse;
    @Mock
    private Selection<ItemReference> selection;
    @Mock
    private DateTimeFormatter        dateTimeFormatter;

    private HistoryPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        when(appContext.getWorkspaceId()).thenReturn("id");

        presenter = new HistoryPresenter(view,
                                         eventBus,
                                         resources,
                                         service,
                                         workspaceAgent,
                                         constant,
                                         appContext,
                                         notificationManager,
                                         dtoUnmarshallerFactory,
                                         dateTimeFormatter,
                                         selectionAgent,
                                         gitOutputConsoleFactory,
                                         consolesPanelPresenter);
        presenter.setPartStack(partStack);

        when(partStack.getActivePart()).thenReturn(activePart);
        when(selectedRevision.getId()).thenReturn(REVISION_ID);
    }

    @Test
    public void testShowDialogWhenLogRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, logResponse);
                return callback;
            }
        }).when(service).log(anyObject(), anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
        verify(view).selectProjectChangesButton(eq(SELECTED_ITEM));
        verify(view).selectDiffWithPrevVersionButton(eq(SELECTED_ITEM));
        verify(view).setCommitADate(eq(EMPTY_TEXT));
        verify(view).setCommitARevision(eq(EMPTY_TEXT));
        verify(view).setCommitBDate(eq(EMPTY_TEXT));
        verify(view).setCommitBRevision(eq(EMPTY_TEXT));
        verify(view).setDiffContext(eq(EMPTY_TEXT));
        verify(view).setCompareType(anyString());
        verify(view).setRevisions((List<Revision>)anyObject());
        verify(workspaceAgent).openPart(eq(presenter), eq(PartStackType.TOOLING));
        verify(partStack).getActivePart();
        verify(partStack).setActivePart(eq(presenter));
    }

    @Test
    public void testShowDialogWhenLogRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).log(anyObject(), anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
        verify(view).selectProjectChangesButton(eq(SELECTED_ITEM));
        verify(view).selectDiffWithPrevVersionButton(eq(SELECTED_ITEM));
        verify(view, times(2)).setCommitADate(eq(EMPTY_TEXT));
        verify(view, times(2)).setCommitARevision(eq(EMPTY_TEXT));
        verify(view, times(2)).setCommitBDate(eq(EMPTY_TEXT));
        verify(view, times(2)).setCommitBRevision(eq(EMPTY_TEXT));
        verify(view, times(2)).setDiffContext(eq(EMPTY_TEXT));
        verify(view, times(2)).setCompareType(anyString());
        verify(workspaceAgent).openPart(eq(presenter), eq(PartStackType.TOOLING));
        verify(partStack).getActivePart();
        verify(partStack).setActivePart(eq(presenter));
        verify(constant, times(2)).historyNothingToDisplay();
        verify(constant).logFailed();
        verify(gitOutputConsoleFactory).create(LOG_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnRefreshClicked() throws Exception {
        presenter.onRefreshClicked();

        verify(appContext).getCurrentProject();
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnProjectChangesClickedWhenDiffRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[7];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service)
          .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());

        presenter.onDiffWithIndexClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        presenter.onProjectChangesClicked();

        verify(view).selectProjectChangesButton(eq(ENABLE_BUTTON));
        verify(view).selectResourceChangesButton(eq(DISABLE_BUTTON));
        verify(service, times(2))
                .diff(anyObject(), eq(rootProjectConfig), (List<String>)anyObject(), eq(RAW), eq(NO_RENAMES), eq(RENAME_LIMIT),
                      eq(REVISION_ID), anyBoolean(), (AsyncRequestCallback<String>)anyObject());
        verify(view).setDiffContext(eq(EMPTY_TEXT));
        verify(constant, times(2)).historyDiffIndexState();
        verify(view).setCommitADate(anyString());
        verify(view).setCommitARevision(anyString());
        verify(view).setCompareType(anyString());
        verify(service, times(3)).log(anyObject(), eq(rootProjectConfig), null, eq(false), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnProjectChangesClickedWhenDiffRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[7];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service)
          .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());

        presenter.onDiffWithIndexClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        presenter.onProjectChangesClicked();

        verify(view).selectProjectChangesButton(eq(ENABLE_BUTTON));
        verify(view).selectResourceChangesButton(eq(DISABLE_BUTTON));
        verify(service, times(2))
                .diff(anyObject(), eq(rootProjectConfig), (List<String>)anyObject(), eq(RAW), eq(NO_RENAMES), eq(RENAME_LIMIT),
                      eq(REVISION_ID), anyBoolean(), (AsyncRequestCallback<String>)anyObject());
        verify(constant, times(2)).diffFailed();
        verify(gitOutputConsoleFactory).create(DIFF_COMMAND_NAME);
        verify(console, times(2)).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(view).setCommitADate(anyString());
        verify(view).setCommitARevision(anyString());
        verify(view).setCommitBDate(eq(EMPTY_TEXT));
        verify(view).setCommitBRevision(eq(EMPTY_TEXT));
        verify(view).setDiffContext(eq(EMPTY_TEXT));
        verify(view).setCompareType(anyString());
        verify(constant, times(2)).historyNothingToDisplay();
        verify(service, times(3)).log(anyObject(), eq(rootProjectConfig), null, eq(false), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnResourceChangesClicked() throws Exception {
        Selection sel = mock(Selection.class);
        ItemReference item = mock(ItemReference.class);
        ActivePartChangedEvent event = mock(ActivePartChangedEvent.class);
        when(event.getActivePart()).thenReturn(activePart);
        when(activePart.getSelection()).thenReturn(sel);
        when(sel.getFirstElement()).thenReturn(item);
        when(selection.getFirstElement()).thenReturn(item);
        when(item.getPath()).thenReturn("testProject/src");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();

                List<String> filePatterns = (List<String>)arguments[1];
                if (filePatterns.size() != 0) {
                    int expected = 1;
                    int actual = filePatterns.size();
                    assertEquals(expected, actual);
                    assertEquals("src", filePatterns.get(0));
                }
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[7];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service)
          .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());

        presenter.showDialog();
        presenter.onDiffWithIndexClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        presenter.onResourceChangesClicked();

        verify(view).selectProjectChangesButton(eq(DISABLE_BUTTON));
        verify(view).selectResourceChangesButton(eq(ENABLE_BUTTON));
    }

    @Test
    public void testOnDiffWithIndexClicked() throws Exception {
        presenter.onDiffWithWorkTreeClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        reset(service);
        presenter.onDiffWithIndexClicked();


        verify(view).selectDiffWithIndexButton(eq(SELECTED_ITEM));
        verify(view).selectDiffWithPrevVersionButton(eq(UNSELECTED_ITEM));
        verify(view).selectDiffWithWorkingTreeButton(eq(UNSELECTED_ITEM));
        verify(service)
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());
        verify(service, never())
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyString(), anyObject());
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnDiffWithIndexTwiceClicked() throws Exception {
        presenter.onDiffWithIndexClicked();
        reset(view);
        reset(service);
        presenter.onDiffWithIndexClicked();

        verify(view, never()).selectDiffWithIndexButton(anyBoolean());
        verify(view, never()).selectDiffWithPrevVersionButton(anyBoolean());
        verify(view, never()).selectDiffWithWorkingTreeButton(anyBoolean());
        verify(service, never())
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());
        verify(service, never()).diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyString(), anyObject());
        verify(service, never())
                .log(anyObject(), eq(projectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnDiffWithWorkTreeClicked() throws Exception {
        presenter.onDiffWithIndexClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        reset(service);
        presenter.onDiffWithWorkTreeClicked();

        verify(view).selectDiffWithWorkingTreeButton(eq(SELECTED_ITEM));
        verify(view).selectDiffWithIndexButton(eq(UNSELECTED_ITEM));
        verify(view).selectDiffWithPrevVersionButton(eq(UNSELECTED_ITEM));
        verify(service).diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());
        verify(service, never())
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyString(), anyObject());
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnDiffWithWorkTreeTwiceClicked() throws Exception {
        presenter.onDiffWithWorkTreeClicked();
        reset(view);
        reset(service);
        presenter.onDiffWithWorkTreeClicked();

        verify(view, never()).selectDiffWithIndexButton(anyBoolean());
        verify(view, never()).selectDiffWithPrevVersionButton(anyBoolean());
        verify(view, never()).selectDiffWithWorkingTreeButton(anyBoolean());
        verify(service, never())
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyObject());
        verify(service, never())
                .diff(anyObject(), anyObject(), anyObject(), anyObject(), anyBoolean(), anyInt(), anyString(), anyString(), anyObject());
        verify(service, never())
                .log(anyObject(), eq(projectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnDiffWithPrevCommitClicked() throws Exception {
        List<Revision> revisions = new ArrayList<Revision>();
        revisions.add(selectedRevision);
        revisions.add(selectedRevision);
        when(logResponse.getCommits()).thenReturn(revisions);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, logResponse);
                return callback;
            }
        }).when(service).log(anyObject(), anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());

        presenter.onRefreshClicked();
        presenter.onDiffWithWorkTreeClicked();
        presenter.onRevisionSelected(selectedRevision);
        reset(view);
        reset(service);
        presenter.onDiffWithPrevCommitClicked();

        verify(view).selectDiffWithPrevVersionButton(eq(SELECTED_ITEM));
        verify(view).selectDiffWithIndexButton(eq(UNSELECTED_ITEM));
        verify(view).selectDiffWithWorkingTreeButton(eq(UNSELECTED_ITEM));
        verify(service, never())
                .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                      anyString(),
                      anyBoolean(), (AsyncRequestCallback<String>)anyObject());
        verify(service)
                .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                      anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());
        verify(service)
                .log(anyObject(), eq(rootProjectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnDiffWithPrevCommitTwiceClicked() throws Exception {
        presenter.onDiffWithPrevCommitClicked();
        reset(view);
        reset(service);
        presenter.onDiffWithPrevCommitClicked();

        verify(view, never()).selectDiffWithIndexButton(anyBoolean());
        verify(view, never()).selectDiffWithPrevVersionButton(anyBoolean());
        verify(view, never()).selectDiffWithWorkingTreeButton(anyBoolean());
        verify(service, never())
                .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                      anyString(),
                      anyBoolean(), (AsyncRequestCallback<String>)anyObject());
        verify(service, never())
                .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                      anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());
        verify(service, never())
                .log(anyObject(), eq(projectConfig), null, eq(TEXT_NOT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
    }

    @Test
    public void testOnRevisionSelectedWhenDiffRequestIsSuccessful() throws Exception {
        List<Revision> revisions = new ArrayList<Revision>();
        revisions.add(selectedRevision);
        revisions.add(selectedRevision);
        when(logResponse.getCommits()).thenReturn(revisions);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, logResponse);
                return callback;
            }
        }).when(service).log(anyObject(), anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[7];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service)
          .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());

        presenter.showDialog();
        reset(view);
        presenter.onRevisionSelected(selectedRevision);

        verify(service)
                .diff(anyObject(), eq(rootProjectConfig), (List<String>)anyObject(), eq(DiffRequest.DiffType.RAW), anyBoolean(), anyInt(),
                      eq(REVISION_ID), eq(REVISION_ID), (AsyncRequestCallback<String>)anyObject());
        verify(view).setDiffContext(eq(EMPTY_TEXT));
        verify(view).setCommitADate(anyString());
        verify(view).setCommitARevision(anyString());
        verify(view).setCommitBDate(anyString());
        verify(view).setCommitBRevision(anyString());
    }

    @Test
    public void testOnRevisionSelectedWhenDiffRequestIsFailed() throws Exception {
        List<Revision> revisions = new ArrayList<Revision>();
        revisions.add(selectedRevision);
        revisions.add(selectedRevision);
        when(logResponse.getCommits()).thenReturn(revisions);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, logResponse);
                return callback;
            }
        }).when(service).log(anyObject(), anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[7];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service)
          .diff(anyObject(), anyObject(), (List<String>)anyObject(), (DiffRequest.DiffType)anyObject(), anyBoolean(), anyInt(),
                anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());

        presenter.showDialog();
        reset(view);
        presenter.onRevisionSelected(selectedRevision);

        verify(service)
                .diff(anyObject(), eq(rootProjectConfig), (List<String>)anyObject(), eq(DiffRequest.DiffType.RAW), anyBoolean(), anyInt(),
                      eq(REVISION_ID), eq(REVISION_ID), (AsyncRequestCallback<String>)anyObject());
        verify(view).setDiffContext(eq(EMPTY_TEXT));
        verify(view).setCommitADate(anyString());
        verify(view).setCommitARevision(anyString());
        verify(view).setCommitBDate(anyString());
        verify(view).setCommitBRevision(anyString());
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(container).setWidget(eq(view));
    }
}
