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
package org.eclipse.che.plugin.svn.ide.commit;

import org.eclipse.che.plugin.svn.ide.commit.diff.DiffViewerPresenter;
import org.eclipse.che.plugin.svn.ide.common.BaseSubversionPresenterTest;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.StatusItem;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.test.GwtReflectionUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.commit.CommitPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class CommitPresenterTest extends BaseSubversionPresenterTest {

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<CLIOutputResponse>> asyncRequestCallbackStatusCaptor;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<CLIOutputWithRevisionResponse>> asyncRequestCallbackCommitCaptor;

    private CommitPresenter presenter;

    @Mock
    DiffViewerPresenter diffViewerPresenter;

    @Mock
    CommitView view;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        presenter =
                new CommitPresenter(appContext, view, dtoUnmarshallerFactory, notificationManager,
                                    subversionOutputConsoleFactory, constants,
                                    service, consolesPanelPresenter, projectExplorerPart, diffViewerPresenter);
    }

    @Test
    public void testCommitViewShouldLoadAllChanges() throws Exception {
        CLIOutputResponse response = mock(CLIOutputResponse.class);

        when(response.getOutput()).thenReturn(Collections.EMPTY_LIST);

        presenter.show();

        verify(service).status(eq("/plugin-svn-test"),
                               anyListOf(String.class),
                               isNull(String.class),
                               eq(false),
                               eq(false),
                               eq(false),
                               eq(true),
                               eq(false),
                               isNull(List.class),
                               asyncRequestCallbackStatusCaptor.capture());

        AsyncRequestCallback<CLIOutputResponse> requestCallback = asyncRequestCallbackStatusCaptor.getValue();
        GwtReflectionUtils.callPrivateMethod(requestCallback, "onSuccess", response);

        verify(view).setChangesList(anyListOf(StatusItem.class));
    }

    @Test
    public void testCommitViwShouldBeClosed() throws Exception {
        presenter.onCancelClicked();

        verify(view).onClose();
    }

    @Test
    public void testCommitAllShouldBeFired() throws Exception {
        when(view.getMessage()).thenReturn("foo");
        when(view.isKeepLocksStateSelected()).thenReturn(false);
        when(view.isCommitAllSelected()).thenReturn(true);

        presenter.onCommitClicked();

        verify(service).commit(eq("/plugin-svn-test"),
                               eq(Collections.singletonList(".")),
                               eq("foo"),
                               eq(false),
                               eq(false),
                               asyncRequestCallbackCommitCaptor.capture());
    }

    @Test
    public void testShowDiffShouldBeFired() throws Exception {
        presenter.showDiff("/path");

        CLIOutputResponse response = mock(CLIOutputResponse.class);

        when(response.getOutput()).thenReturn(Collections.EMPTY_LIST);

        verify(service).showDiff(eq("/plugin-svn-test"),
                                 eq(Collections.singletonList("/path")),
                                 eq("HEAD"),
                                 asyncRequestCallbackStatusCaptor.capture());
        AsyncRequestCallback<CLIOutputResponse> requestCallback = asyncRequestCallbackStatusCaptor.getValue();
        GwtReflectionUtils.callPrivateMethod(requestCallback, "onSuccess", response);

        verify(diffViewerPresenter).showDiff("");
    }
}
