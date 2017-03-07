/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.targets.categories.docker;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsTreeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Oleksii Orel */
@RunWith(MockitoJUnitRunner.class)
public class DockerCategoryPresenterTest {
    @Mock
    private DockerView                  dockerView;
    @Mock
    private DialogFactory               dialogFactory;
    @Mock
    private NotificationManager         notificationManager;
    @Mock
    private MachineLocalizationConstant machineLocale;
    @Mock
    private EventBus                    eventBus;


    //additional mocks
    @Mock
    private TargetsTreeManager targetsTreeManager;

    @Mock
    private ConfirmDialog confirmDialog;

    @Mock
    private Promise<Void> promise;

    @Captor
    private ArgumentCaptor<ConfirmCallback> confirmCaptor;

    @Captor
    private ArgumentCaptor<Operation<Void>> operationSuccessCapture;


    private DockerCategoryPresenter arbitraryCategoryPresenter;

    @Before
    public void setUp() {
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        when(promise.then(operationSuccessCapture.capture())).thenReturn(promise);

        arbitraryCategoryPresenter = new DockerCategoryPresenter(dockerView, machineLocale);

        arbitraryCategoryPresenter.setTargetsTreeManager(targetsTreeManager);
    }



    @Test
    public void testGetCategory() throws Exception {
        arbitraryCategoryPresenter.getCategory();

        verify(machineLocale).targetsViewCategoryDocker();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        arbitraryCategoryPresenter.go(container);

        verify(container).setWidget(eq(dockerView));
    }
}
