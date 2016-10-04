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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerPresenterTest {

    //constructor mocks
    @Mock
    private ServerView    view;
    @Mock
    private EntityFactory entityFactory;

    //additional mocks
    @Mock
    private MachineEntity    machine;
    @Mock
    private AcceptsOneWidget container;

    @Captor
    private ArgumentCaptor<List<ServerEntity>> serverListCaptor;

    @InjectMocks
    private ServerPresenter presenter;

    @Test
    public void serverShouldBeUpdated() {
        presenter.updateInfo(machine);

        verify(machine).getRuntime();
        verify(view).setServers(Matchers.<List<ServerEntity>>anyObject());
    }

    @Test
    public void terminalShouldBeDisplayed() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void terminalVisibilityShouldBeChanged() {
        presenter.setVisible(true);

        verify(view).setVisible(true);
    }
}
