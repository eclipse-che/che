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
package org.eclipse.che.ide.workspace.create;

import com.google.gwt.core.client.Callback;
import com.google.inject.Provider;

import org.eclipse.che.api.machine.gwt.client.RecipeServiceClient;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentStateDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.bootstrap.DefaultWorkspaceComponent;
import org.eclipse.che.ide.bootstrap.WorkspaceComponent;
import org.eclipse.che.ide.core.Component;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.workspace.create.CreateWorkspaceView.HidePopupCallBack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter.MAX_COUNT;
import static org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter.RECIPE_TYPE;
import static org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter.SKIP_COUNT;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateWorkspacePresenterTest {
    //constructor mocks
    @Mock
    private CreateWorkspaceView          view;
    @Mock
    private DtoFactory                   dtoFactory;
    @Mock
    private WorkspaceServiceClient       workspaceClient;
    @Mock
    private CoreLocalizationConstant     locale;
    @Mock
    private Provider<WorkspaceComponent> wsComponentProvider;
    @Mock
    private RecipeServiceClient          recipeServiceClient;
    @Mock
    private BrowserQueryFieldRenderer    browserQueryFieldRenderer;

    //additional mocks
    @Mock
    private Callback<Component, Exception>  componentCallback;
    @Mock
    private HidePopupCallBack               popupCallBack;
    @Mock
    private Promise<List<RecipeDescriptor>> recipesPromise;
    @Mock
    private Promise<UsersWorkspaceDto>      userWsPromise;
    @Mock
    private RecipeDescriptor                recipeDescriptor;
    @Mock
    private DefaultWorkspaceComponent       workspaceComponent;
    @Mock
    private LimitsDto                       limitsDto;

    //DTOs
    @Mock
    private MachineStateDto     machineConfigDto;
    @Mock
    private MachineSourceDto    machineSourceDto;
    @Mock
    private EnvironmentDto      environmentDto;
    @Mock
    private EnvironmentStateDto environmentStateDto;
    @Mock
    private CommandDto          commandDto;
    @Mock
    private WorkspaceConfigDto  workspaceConfigDto;
    @Mock
    private UsersWorkspaceDto   usersWorkspaceDto;

    @Captor
    private ArgumentCaptor<Operation<List<RecipeDescriptor>>> recipeOperation;
    @Captor
    private ArgumentCaptor<Operation<UsersWorkspaceDto>>      workspaceOperation;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>           errorOperation;

    @InjectMocks
    private CreateWorkspacePresenter presenter;

    @Before
    public void setUp() {
        when(dtoFactory.createDto(MachineSourceDto.class)).thenReturn(machineSourceDto);
        when(machineSourceDto.withType(anyString())).thenReturn(machineSourceDto);
        when(machineSourceDto.withLocation(anyString())).thenReturn(machineSourceDto);

        when(dtoFactory.createDto(LimitsDto.class)).thenReturn(limitsDto);
        when(limitsDto.withRam(anyInt())).thenReturn(limitsDto);

        when(dtoFactory.createDto(MachineConfigDto.class)).thenReturn(machineConfigDto);
        when(machineConfigDto.withName(anyString())).thenReturn(machineConfigDto);
        when(machineConfigDto.withType(anyString())).thenReturn(machineConfigDto);
        when(machineConfigDto.withSource(machineSourceDto)).thenReturn(machineConfigDto);
        when(machineConfigDto.withDev(anyBoolean())).thenReturn(machineConfigDto);
        when(machineConfigDto.withLimits(limitsDto)).thenReturn(machineConfigDto);

        when(dtoFactory.createDto(EnvironmentDto.class)).thenReturn(environmentDto);
        when(environmentDto.withName(anyString())).thenReturn(environmentDto);
        when(environmentDto.withMachineConfigs(Matchers.<List<MachineConfigDto>>anyObject())).thenReturn(environmentDto);

        when(dtoFactory.createDto(WorkspaceConfigDto.class)).thenReturn(workspaceConfigDto);
        when(workspaceConfigDto.withName(anyString())).thenReturn(workspaceConfigDto);
        when(workspaceConfigDto.withDefaultEnv(anyString())).thenReturn(workspaceConfigDto);
        when(workspaceConfigDto.withEnvironments(Matchers.<List<EnvironmentDto>>anyObject())).thenReturn(workspaceConfigDto);
        when(workspaceConfigDto.withCommands(Matchers.<List<CommandDto>>anyObject())).thenReturn(workspaceConfigDto);
        when(workspaceConfigDto.withAttributes(Matchers.<Map<String, String>>anyObject())).thenReturn(workspaceConfigDto);

        when(dtoFactory.createDto(UsersWorkspaceDto.class)).thenReturn(usersWorkspaceDto);
        when(usersWorkspaceDto.withName(anyString())).thenReturn(usersWorkspaceDto);
        when(usersWorkspaceDto.withDefaultEnv(anyString())).thenReturn(usersWorkspaceDto);
        when(usersWorkspaceDto.withEnvironments(Matchers.<List<EnvironmentStateDto>>anyObject())).thenReturn(usersWorkspaceDto);

        when(wsComponentProvider.get()).thenReturn(workspaceComponent);

        when(recipeServiceClient.getRecipes(anyInt(), anyInt())).thenReturn(recipesPromise);
        when(view.getWorkspaceName()).thenReturn("test");
        when(view.getRecipeUrl()).thenReturn("recipe");
    }

    @Test
    public void delegateShouldBeSet() {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void dialogShouldBeShown() {
        presenter.show(Arrays.asList(usersWorkspaceDto), componentCallback);

        verify(browserQueryFieldRenderer).getWorkspaceName();
        verify(view).setWorkspaceName(anyString());

        verify(view).show();
    }

    @Test
    public void errorLabelShouldBeShownWhenWorkspaceNameLengthIsInCorrect() {
        when(view.getWorkspaceName()).thenReturn("te");

        presenter.onNameChanged();

        verify(locale).createWsNameLengthIsNotCorrect();
        verify(locale, never()).createWsNameIsNotCorrect();
        verify(locale, never()).createWsNameAlreadyExist();
    }

    @Test
    public void errorLabelShouldBeShownWhenWorkspaceNameIsInCorrect() {
        when(view.getWorkspaceName()).thenReturn("test/*");

        presenter.onNameChanged();

        verify(locale, never()).createWsNameLengthIsNotCorrect();
        verify(locale).createWsNameIsNotCorrect();
        verify(locale, never()).createWsNameAlreadyExist();
    }

    @Test
    public void errorLabelShouldBeShownWhenWorkspaceNameAlreadyExist() {
        when(usersWorkspaceDto.getName()).thenReturn("test");

        presenter.show(Arrays.asList(usersWorkspaceDto), componentCallback);
        reset(locale);

        presenter.onNameChanged();

        verify(locale, never()).createWsNameLengthIsNotCorrect();
        verify(locale, never()).createWsNameIsNotCorrect();
        verify(locale).createWsNameAlreadyExist();
    }


    @Test
    public void errorLabelShouldNotBeShownWhenFormIsCorrect() {
        when(view.getRecipeUrl()).thenReturn("http://localhost/correct/url");

        presenter.onRecipeUrlChanged();

        verify(locale, never()).createWsNameLengthIsNotCorrect();
        verify(locale, never()).createWsNameIsNotCorrect();
        verify(locale, never()).createWsNameAlreadyExist();

        verify(view).showValidationNameError("");

        verify(view).setVisibleUrlError(false);

        verify(view).setEnableCreateButton(true);
    }

    @Test
    public void errorLabelShouldBeShownWhenRecipeUrlIsNotCorrect() {
        when(view.getRecipeUrl()).thenReturn("xxx://localh/correct/url");

        presenter.onRecipeUrlChanged();

        verify(locale, never()).createWsNameLengthIsNotCorrect();
        verify(locale, never()).createWsNameIsNotCorrect();
        verify(locale, never()).createWsNameAlreadyExist();

        verify(view).showValidationNameError("");

        verify(view).setVisibleUrlError(anyBoolean());
    }

    @Test
    public void recipesShouldBeFoundAndShown() throws Exception {
        List<RecipeDescriptor> recipes = Arrays.asList(recipeDescriptor);

        callSearchRecipesApplyMethod(recipes);

        verify(popupCallBack, never()).hidePopup();
        verify(view).showFoundByTagRecipes(recipes);
        verify(view).setVisibleTagsError(false);
    }

    private void callSearchRecipesApplyMethod(List<RecipeDescriptor> recipes) throws Exception {
        List<String> tags = Arrays.asList("test1 test2");

        when(view.getTags()).thenReturn(tags);
        when(recipeServiceClient.searchRecipes(Matchers.<List<String>>anyObject(),
                                               anyString(),
                                               anyInt(),
                                               anyInt())).thenReturn(recipesPromise);

        presenter.onTagsChanged(popupCallBack);

        verify(view).getTags();
        verify(recipeServiceClient).searchRecipes(tags, RECIPE_TYPE, SKIP_COUNT, MAX_COUNT);
        verify(recipesPromise).then(recipeOperation.capture());

        recipeOperation.getValue().apply(recipes);
    }

    @Test
    public void errorLabelShouldBeShowWhenRecipesNotFound() throws Exception {
        List<RecipeDescriptor> recipes = new ArrayList<>();

        callSearchRecipesApplyMethod(recipes);

        verify(view).setVisibleTagsError(true);
        verify(popupCallBack).hidePopup();
        verify(view, never()).showFoundByTagRecipes(Matchers.<List<RecipeDescriptor>>anyObject());
    }

    @Test
    public void predefinedRecipesShouldBeFound() {
        presenter.onPredefinedRecipesClicked();

        verify(view).showPredefinedRecipes(Matchers.<List<RecipeDescriptor>>anyObject());
    }

    @Test
    public void dialogShouldBeHiddenWhenUserClicksOnCreateButton() {
        clickOnCreateButton();

        verify(view).hide();
    }

    private void clickOnCreateButton() {
        when(workspaceClient.create(Matchers.<WorkspaceConfigDto>anyObject(), anyString())).thenReturn(userWsPromise);
        when(userWsPromise.then(Matchers.<Operation<UsersWorkspaceDto>>anyObject())).thenReturn(userWsPromise);
        when(userWsPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(userWsPromise);
        when(recipeServiceClient.getRecipes(anyInt(), anyInt())).thenReturn(recipesPromise);

        presenter.show(Arrays.asList(usersWorkspaceDto), componentCallback);

        presenter.onCreateButtonClicked();

        verify(recipeServiceClient).getRecipes(anyInt(), anyInt());
        verify(recipesPromise).then(Matchers.<Operation<List<RecipeDescriptor>>>anyObject());

        verify(view).show();
    }

    @Test
    public void workspaceConfigShouldBeGot() {
        when(view.getWorkspaceName()).thenReturn("name");
        when(view.getRecipeUrl()).thenReturn("test");

        clickOnCreateButton();

        verify(view, times(2)).getWorkspaceName();
        verify(dtoFactory).createDto(MachineConfigDto.class);
        verify(machineConfigDto).withName("ws-machine");
        verify(machineConfigDto).withType("docker");
        verify(machineConfigDto).withSource(machineSourceDto);
        verify(machineConfigDto).withDev(true);

        verify(dtoFactory).createDto(MachineSourceDto.class);
        verify(machineSourceDto).withType("recipe");
        verify(machineSourceDto).withLocation("test");

        verify(dtoFactory).createDto(EnvironmentDto.class);
        verify(environmentDto).withName("name");
        verify(environmentDto).withMachineConfigs(Matchers.<List<MachineConfigDto>>anyObject());
    }

    @Test
    public void workspaceShouldBeCreatedForDevMachine() throws Exception {
        when(machineConfigDto.isDev()).thenReturn(true);

        callApplyCreateWorkspaceMethod();

        verify(wsComponentProvider).get();
        verify(workspaceComponent).startWorkspaceById(usersWorkspaceDto);
    }

    private void callApplyCreateWorkspaceMethod() throws Exception {
        List<EnvironmentStateDto> environments = new ArrayList<>();
        environments.add(environmentStateDto);

        when(usersWorkspaceDto.getDefaultEnv()).thenReturn("name");
        when(usersWorkspaceDto.getEnvironments()).thenReturn(environments);

        when(environmentDto.getMachineConfigs()).thenReturn(Arrays.<MachineConfigDto>asList(machineConfigDto));

        clickOnCreateButton();

        verify(userWsPromise).then(workspaceOperation.capture());
        workspaceOperation.getValue().apply(usersWorkspaceDto);
    }

    @Test
    public void workspaceShouldBeCreatedForNotDevMachine() throws Exception {
        when(machineConfigDto.isDev()).thenReturn(false);

        callApplyCreateWorkspaceMethod();

        verify(machineConfigDto, never()).getChannels();

        verify(workspaceComponent).startWorkspaceById(usersWorkspaceDto);
    }

    @Test
    public void errorShouldBeCaughtWhenCreatesWorkSpace() throws Exception {
        final PromiseError promiseError = mock(PromiseError.class);

        clickOnCreateButton();

        verify(userWsPromise).catchError(errorOperation.capture());
        errorOperation.getValue().apply(promiseError);

        //noinspection ThrowableResultOfMethodCallIgnored
        verify(promiseError).getCause();
        verify(componentCallback).onFailure(Matchers.<Exception>anyObject());
    }
}