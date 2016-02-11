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
package org.eclipse.che.ide.ui.loaders.initialization;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.WORKING;
import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.COMPLETED;
import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.LoaderStateListener;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.ERROR;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;


/**
 * Testing {@link LoaderPresenter} functionality.
 *
 * @author Roman Nikitenko.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoaderPresenterTest {
    LoaderPresenter.LoaderStateListener listener;
    private static final String OPERATION = "Booting workspace machine";
    @Mock
    private LoaderView      view;
    @InjectMocks
    private LoaderPresenter presenter;

    @Before
    public void setUp() {
        listener = mock(LoaderStateListener.class);
        presenter.setListener(listener);
    }

    @Test
    public void showLoaderTest() {
        LoadingInfo loadingInfo = mock(LoadingInfo.class);

        presenter.show(loadingInfo);

        verify(loadingInfo).getOperations();
        verify(loadingInfo).getDisplayNames();
        verify(view).setOperations(anyList());
        verify(listener).onLoaderStateChanged(eq(WORKING));
    }

    @Test
    public void onStatusChangedWhenInProgressTest() {
        OperationInfo operationInfo = mock(OperationInfo.class);
        presenter.operations = new ArrayList<>();
        presenter.operations.add(operationInfo);
        int index = presenter.operations.indexOf(operationInfo);
        when(operationInfo.getStatus()).thenReturn(IN_PROGRESS);
        when(operationInfo.getOperationName()).thenReturn(OPERATION);

        presenter.onStatusChanged(operationInfo);

        verify(view).setInProgressStatus(eq(index), eq(OPERATION));
        verify(view).setCurrentOperation(eq(OPERATION));
        verify(view, never()).setErrorStatus(anyInt(), anyString());
        verify(view, never()).setSuccessStatus(anyInt(), anyString());
    }

    @Test
    public void onStatusChangedWhenSuccessTest() {
        OperationInfo operationInfo = mock(OperationInfo.class);
        presenter.operations = new ArrayList<>();
        presenter.operations.add(operationInfo);
        int index = presenter.operations.indexOf(operationInfo);
        when(operationInfo.getStatus()).thenReturn(SUCCESS);
        when(operationInfo.getOperationName()).thenReturn(OPERATION);

        presenter.onStatusChanged(operationInfo);

        verify(view).setSuccessStatus(eq(index), eq(OPERATION));
        verify(view, never()).setErrorStatus(anyInt(), anyString());
        verify(view, never()).setInProgressStatus(anyInt(), anyString());
    }

    @Test
    public void onStatusChangedWhenErrorTest() {
        OperationInfo operationInfo = mock(OperationInfo.class);
        presenter.operations = new ArrayList<>();
        presenter.operations.add(operationInfo);
        int index = presenter.operations.indexOf(operationInfo);
        when(operationInfo.getStatus()).thenReturn(ERROR);
        when(operationInfo.getOperationName()).thenReturn(OPERATION);

        presenter.onStatusChanged(operationInfo);

        verify(view).setErrorStatus(eq(index), eq(OPERATION));
        verify(view).setCurrentOperation(eq("Error while " + OPERATION));
        verify(view, never()).setSuccessStatus(anyInt(), anyString());
        verify(view, never()).setInProgressStatus(anyInt(), anyString());
    }

    @Test
    public void updateStateTest() {
        String successOperationName = "Success";
        OperationInfo successOperation = mock(OperationInfo.class);
        OperationInfo inProgressOperation = mock(OperationInfo.class);
        presenter.operations = new ArrayList<>();
        presenter.operations.add(successOperation);
        presenter.operations.add(inProgressOperation);
        int completedState = 100 / presenter.operations.size();
        when(successOperation.getStatus()).thenReturn(SUCCESS);
        when(inProgressOperation.getStatus()).thenReturn(IN_PROGRESS);
        when(inProgressOperation.getOperationName()).thenReturn(successOperationName);

        presenter.onStatusChanged(successOperation);

        verify(view).setCurrentOperation(eq(successOperationName));
        verify(view).setProgressBarState(eq(completedState));
    }

    @Test
    public void hideLoaderTest() {
        presenter.hide();

        verify(listener).onLoaderStateChanged(eq(COMPLETED));
    }

    @Test
    public void onExpanderClickedWhenDetailsPanelClosedTest() {
        presenter.expandPanelState = false;

        presenter.onExpanderClicked();

        verify(view).expandOperations();
    }

    @Test
    public void onExpanderClickedWhenDetailsPanelOpenedTest() {
        presenter.expandPanelState = true;

        presenter.onExpanderClicked();

        verify(view).collapseOperations();
    }

    @Test
    public void getCustomComponentTest() throws Exception {
        presenter.getCustomComponent();

        verify(view).asWidget();
    }
}
