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

import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
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
    private Machine          machine;
    @Mock
    private AcceptsOneWidget container;

    @Captor
    private ArgumentCaptor<List<Server>> serverListCaptor;

    @InjectMocks
    private ServerPresenter presenter;

    @Test
    public void serverShouldBeUpdated() {
        presenter.updateInfo(machine);

        verify(machine).getServersList();
        verify(view).setServers(Matchers.<List<Server>>anyObject());
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