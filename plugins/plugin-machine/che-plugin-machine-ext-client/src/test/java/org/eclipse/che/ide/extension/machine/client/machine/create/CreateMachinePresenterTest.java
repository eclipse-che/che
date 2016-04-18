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
package org.eclipse.che.ide.extension.machine.client.machine.create;

import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.project.gwt.client.ProjectTypeServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynny */
@RunWith(MockitoJUnitRunner.class)
public class CreateMachinePresenterTest {

    private final static String RECIPE_URL   = "http://www.host.com/recipe";
    private final static String MACHINE_NAME = "machine";

    private final static String SOME_TEXT = "someText";

    @Mock
    private CreateMachineView        view;
    @Mock
    private MachineManager           machineManager;
    @Mock
    private AppContext               appContext;
    @Mock
    private ProjectTypeServiceClient projectTypeServiceClient;
    @Mock
    private MachineServiceClient     machineServiceClient;
    @Mock
    private EntityFactory            entityFactory;

    @InjectMocks
    private CreateMachinePresenter presenter;

    @Mock
    private Promise<MachineDto>                   machineDescriptorPromise;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>> machineCaptor;

    @Before
    public void setUp() {
        when(view.getRecipeURL()).thenReturn(RECIPE_URL);
        when(view.getMachineName()).thenReturn(MACHINE_NAME);
    }

    @Test
    public void shouldSetActionDelegate() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void viewShouldBeShown() throws Exception {
        presenter.showDialog();

        verify(view).show();
        verify(view).setCreateButtonState(false);
        verify(view).setReplaceButtonState(false);
        verify(view).setMachineName("");
        verify(view).setRecipeURL("");
        verify(view).setErrorHint(false);
        verify(view).setTags("");
    }

    @Test
    public void buttonsShouldBeDisabledWhenNameIsEmpty() throws Exception {
        when(view.getMachineName()).thenReturn("");

        presenter.onNameChanged();

        verify(view).setCreateButtonState(eq(false));
        verify(view).setReplaceButtonState(eq(false));
    }

    @Test
    public void buttonsShouldBeEnabledWhenNameIsNotEmpty() throws Exception {
        presenter.onNameChanged();

        verify(view).setCreateButtonState(eq(true));
        verify(view).setReplaceButtonState(eq(true));
    }

    @Test
    public void shouldCreateMachine() throws Exception {
        presenter.onCreateClicked();

        verify(view).getRecipeURL();
        verify(view).getMachineName();
        verify(machineManager).startMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
        verify(view).close();
    }

    @Test
    public void shouldReplaceDevMachine() throws Exception {
        DevMachine devMachine = mock(DevMachine.class);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getId()).thenReturn(SOME_TEXT);
        when(machineServiceClient.getMachine(SOME_TEXT)).thenReturn(machineDescriptorPromise);

        presenter.onReplaceDevMachineClicked();

        verify(view).getMachineName();
        verify(view).getRecipeURL();
        verify(appContext, times(2)).getDevMachine();
        verify(machineServiceClient).getMachine(SOME_TEXT);
        verify(machineDescriptorPromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(mock(MachineDto.class));
        verify(machineManager).destroyMachine(any(MachineDto.class));
        verify(machineManager).startDevMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
        verify(view).close();
    }

    @Test
    public void shouldStartNewDevMachine() throws Exception {
        when(appContext.getDevMachine()).thenReturn(null);
        when(machineServiceClient.getMachine(SOME_TEXT)).thenReturn(machineDescriptorPromise);

        presenter.onReplaceDevMachineClicked();

        verify(view).getMachineName();
        verify(view).getRecipeURL();
        verify(appContext).getDevMachine();
        verify(machineManager).startDevMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
        verify(view).close();
        verify(machineServiceClient, never()).getMachine(SOME_TEXT);
        verify(machineManager, never()).destroyMachine(any(MachineDto.class));
        verify(machineManager).startDevMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
    }

    @Test
    public void shouldCloseView() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }
}
