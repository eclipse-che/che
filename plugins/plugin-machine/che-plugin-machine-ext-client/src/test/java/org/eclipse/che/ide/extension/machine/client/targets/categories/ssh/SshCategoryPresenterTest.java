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
package org.eclipse.che.ide.extension.machine.client.targets.categories.ssh;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Oleksii Orel */
@RunWith(MockitoJUnitRunner.class)
public class SshCategoryPresenterTest {
    @Mock
    private SshView                     sshView;
    @Mock
    private RecipeServiceClient         recipeServiceClient;
    @Mock
    private DtoFactory                  dtoFactory;
    @Mock
    private DialogFactory               dialogFactory;
    @Mock
    private NotificationManager         notificationManager;
    @Mock
    private MachineLocalizationConstant machineLocale;
    @Mock
    private WorkspaceServiceClient      workspaceServiceClient;
    @Mock
    private AppContext                  appContext;
    @Mock
    private MachineServiceClient        machineService;
    @Mock
    private EventBus                    eventBus;
    @Mock
    private EntityFactory               entityFactory;

    //additional mocks
    @Mock
    private SshMachineTarget   target;
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


    private SshCategoryPresenter arbitraryCategoryPresenter;

    @Before
    public void setUp() {
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        when(promise.then(operationSuccessCapture.capture())).thenReturn(promise);


        arbitraryCategoryPresenter = new SshCategoryPresenter(sshView,
                                                              recipeServiceClient,
                                                              dtoFactory,
                                                              dialogFactory,
                                                              entityFactory,
                                                              notificationManager,
                                                              machineLocale,
                                                              workspaceServiceClient,
                                                              appContext,
                                                              machineService,
                                                              eventBus);
        arbitraryCategoryPresenter.setTargetsTreeManager(targetsTreeManager);
        arbitraryCategoryPresenter.setCurrentSelection(target);
    }


    @Test
    public void testOnDeleteTarget() throws Exception {
        final String deletingTargetName = "deletingTargetName";
        final String recipeId = "deletingTargetRecipeId";
        final String deleteProposal = "Are you sure you want to delete target " + deletingTargetName + " ?";
        final String deleteSuccessMessage = "Target recipe " + deletingTargetName + " successfully deleted";
        final SshMachineTarget target = Mockito.mock(SshMachineTarget.class);
        final RecipeDescriptor recipe = Mockito.mock(RecipeDescriptor.class);
        when(target.getName()).thenReturn(deletingTargetName);
        when(target.getRecipe()).thenReturn(recipe);
        when(recipe.getId()).thenReturn(recipeId);
        when(recipeServiceClient.removeRecipe(recipeId)).thenReturn(promise);
        when(machineLocale.targetsViewDeleteConfirm(deletingTargetName)).thenReturn(deleteProposal);
        when(machineLocale.targetsRecipeDeleteSuccess(deletingTargetName)).thenReturn(deleteSuccessMessage);

        arbitraryCategoryPresenter.onDeleteClicked(target);

        verify(dialogFactory).createConfirmDialog(anyString(), eq(deleteProposal), confirmCaptor.capture(),
                                                  Matchers.<CancelCallback>anyObject());

        confirmCaptor.getValue().accepted();

        verify(recipeServiceClient).removeRecipe(recipeId);
        verify(promise).then(operationSuccessCapture.capture());

        operationSuccessCapture.getValue().apply(null);

        verify(notificationManager).notify(eq(deleteSuccessMessage), eq(SUCCESS), eq(FLOAT_MODE));
        verify(target).isConnected();
        verify(targetsTreeManager).updateTargets(null);
    }


    @Test
    public void testOnCancelClicked() throws Exception {

        arbitraryCategoryPresenter.onCancelClicked();

        verify(sshView).restoreTargetFields(target);
        verify(target).setDirty(false);
        verify(sshView, times(2)).updateTargetFields(target);
    }

    @Test
    public void testDisconnectButtonTitle() throws Exception {
        final String disconnectButtonTitle = "Disconnect";
        when(target.isConnected()).thenReturn(true);

        arbitraryCategoryPresenter.updateButtons(true);

        verify(sshView).setConnectButtonText(disconnectButtonTitle);
    }

    @Test
    public void testConnectButtonTitle() throws Exception {
        final String connectButtonTitle = "Connect";
        when(target.isConnected()).thenReturn(false);

        arbitraryCategoryPresenter.updateButtons(true);

        verify(sshView, times(2)).setConnectButtonText(connectButtonTitle);
    }

    @Test
    public void testOnUserNameChangedFail() throws Exception {
        final String oldTargetUserName = "oldTargetUserName";
        when(target.getUserName()).thenReturn(oldTargetUserName);

        arbitraryCategoryPresenter.onUserNameChanged(oldTargetUserName);

        verify(target, never()).setUserName(oldTargetUserName);
        verify(target, never()).setDirty(true);
    }

    @Test
    public void testOnUserNameChanged() throws Exception {
        final String oldTargetUserName = "oldTargetUserName";
        final String newTargetUserName = "newTargetUserName";
        when(target.getUserName()).thenReturn(oldTargetUserName);

        arbitraryCategoryPresenter.onUserNameChanged(newTargetUserName);

        verify(target).setUserName(newTargetUserName);
        verify(target).setDirty(true);
    }

    @Test
    public void testOnPasswordChangedFail() throws Exception {
        final String targetPassword = "oldTargetPassword";
        when(target.getPassword()).thenReturn(targetPassword);

        arbitraryCategoryPresenter.onPasswordChanged(targetPassword);

        verify(target, never()).setPassword(targetPassword);
        verify(target, never()).setDirty(true);
    }

    @Test
    public void testOnPasswordChanged() throws Exception {
        final String oldTargetPassword = "oldTargetPassword";
        final String newTargetPassword = "newTargetPassword";
        when(target.getPassword()).thenReturn(oldTargetPassword);

        arbitraryCategoryPresenter.onPasswordChanged(newTargetPassword);

        verify(target).setPassword(newTargetPassword);
        verify(target).setDirty(true);
    }

    @Test
    public void testOnHostChangedFail() throws Exception {
        final String targetPort = "oldTargetPort";
        when(target.getHost()).thenReturn(targetPort);

        arbitraryCategoryPresenter.onHostChanged(targetPort);

        verify(target, never()).setHost(targetPort);
        verify(target, never()).setDirty(true);
    }

    @Test
    public void testOnHostChanged() throws Exception {
        final String oldTargetHost = "oldTargetPort";
        final String newTargetHost = "newTargetPort";
        when(target.getHost()).thenReturn(oldTargetHost);

        arbitraryCategoryPresenter.onHostChanged(newTargetHost);

        verify(target).setHost(newTargetHost);
        verify(target).setDirty(true);
        verify(sshView, times(2)).unmarkHost();
    }

    @Test
    public void testOnPortChangedFail() throws Exception {
        final String targetPort = "oldTargetPort";
        when(target.getPort()).thenReturn(targetPort);

        arbitraryCategoryPresenter.onPortChanged(targetPort);

        verify(target, never()).setPort(targetPort);
        verify(target, never()).setDirty(true);
    }

    @Test
    public void testOnPortChanged() throws Exception {
        final String oldTargetPort = "oldTargetPort";
        final String newTargetPort = "newTargetPort";
        when(target.getPort()).thenReturn(oldTargetPort);

        arbitraryCategoryPresenter.onPortChanged(newTargetPort);

        verify(target).setPort(newTargetPort);
        verify(target).setDirty(true);
        verify(sshView, times(2)).unmarkPort();
    }

    @Test
    public void testOnTargetNameChangeFail() throws Exception {
        final String targetName = "oldTargetName";
        when(target.getName()).thenReturn(targetName);

        arbitraryCategoryPresenter.onTargetNameChanged(targetName);

        verify(target, never()).setName(targetName);
    }

    @Test
    public void testOnTargetNameChanged() throws Exception {
        final String oldTargetName = "oldTargetName";
        final String newTargetName = "newTargetName";
        when(target.getName()).thenReturn(oldTargetName);

        arbitraryCategoryPresenter.onTargetNameChanged(newTargetName);

        verify(target).setName(newTargetName);
        verify(target).setDirty(true);
        verify(sshView, times(2)).unmarkTargetName();
    }

    @Test
    public void testGetCategory() throws Exception {
        arbitraryCategoryPresenter.getCategory();

        verify(machineLocale).targetsViewCategorySsh();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        arbitraryCategoryPresenter.go(container);

        verify(container).setWidget(eq(sshView));
    }
}
