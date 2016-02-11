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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessesPresenterTest {

    private static final String MACHINE_ID = "someId";

    //constructor mocks
    @Mock
    private ProcessesView        view;
    @Mock
    private MachineServiceClient service;

    //additional mocks
    @Mock
    private Promise<List<MachineProcessDto>> processPromise;
    @Mock
    private List<MachineProcessDto>          descriptors;
    @Mock
    private AcceptsOneWidget                 container;

    @Captor
    private ArgumentCaptor<Operation<List<MachineProcessDto>>> operationCaptor;

    @InjectMocks
    private ProcessesPresenter presenter;

    @Test
    public void constructorShouldBeVerified() {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void processesShouldBeGot() throws Exception {
        when(service.getProcesses(MACHINE_ID)).thenReturn(processPromise);

        presenter.showProcesses(MACHINE_ID);

        verify(service).getProcesses(MACHINE_ID);

        verify(processPromise).then(operationCaptor.capture());
        operationCaptor.getValue().apply(descriptors);

        verify(view).setProcesses(descriptors);
    }

    @Test
    public void viewShouldBeAddedToContainer() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void viewShouldBeReturned() {
        ProcessesView widget = (ProcessesView)presenter.getView();

        assertThat(widget, sameInstance(view));
    }

    @Test
    public void viewVisibilityShouldBeChanged() {
        presenter.setVisible(true);

        verify(view).setVisible(true);
    }

}