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

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.workspace.DefaultWorkspaceComponent;
import org.eclipse.che.ide.workspace.WorkspaceComponent;
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
import java.util.Collections;
import java.util.HashMap;
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
    private Promise<WorkspaceDto>           userWsPromise;
    @Mock
    private RecipeDescriptor                recipeDescriptor;
    @Mock
    private DefaultWorkspaceComponent       workspaceComponent;
    @Mock
    private LimitsDto                       limitsDto;

    //DTOs
    private MachineConfigDto   machineConfigDto;
    private WorkspaceConfigDto workspaceConfigDto;
    @Mock
    private MachineDto         machineDto;
    @Mock
    private MachineSourceDto   machineSourceDto;
    // Mocks too
    private EnvironmentDto       environmentDto;
    private EnvironmentRecipeDto environmentRecipeDto;
    private ExtendedMachineDto   extendedMachineDto;
    @Mock
    private CommandDto         commandDto;
    @Mock
    private WorkspaceDto  usersWorkspaceDto;



    @Captor
    private ArgumentCaptor<Operation<List<RecipeDescriptor>>> recipeOperation;
    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>>      workspaceOperation;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>           errorOperation;

    @InjectMocks
    private CreateWorkspacePresenter presenter;

    @Before
    public void setUp() {
        machineConfigDto = mock(MachineConfigDto.class, new SelfReturningAnswer());
        workspaceConfigDto = mock(WorkspaceConfigDto.class, new SelfReturningAnswer());
        when(usersWorkspaceDto.getConfig()).thenReturn(workspaceConfigDto);

        when(dtoFactory.createDto(MachineSourceDto.class)).thenReturn(machineSourceDto);
        when(machineSourceDto.withType(anyString())).thenReturn(machineSourceDto);
        when(machineSourceDto.withLocation(anyString())).thenReturn(machineSourceDto);

        when(dtoFactory.createDto(LimitsDto.class)).thenReturn(limitsDto);
        when(limitsDto.withRam(anyInt())).thenReturn(limitsDto);

        when(dtoFactory.createDto(MachineConfigDto.class)).thenReturn(machineConfigDto);

        when(dtoFactory.createDto(EnvironmentDto.class)).thenReturn(environmentDto);

        when(dtoFactory.createDto(WorkspaceConfigDto.class)).thenReturn(workspaceConfigDto);

        when(dtoFactory.createDto(WorkspaceDto.class)).thenReturn(usersWorkspaceDto);
        environmentDto = mock(EnvironmentDto.class, new SelfReturningAnswer());
        when(dtoFactory.createDto(EnvironmentDto.class)).thenReturn(environmentDto);
        environmentRecipeDto = mock(EnvironmentRecipeDto.class, new SelfReturningAnswer());
        when(dtoFactory.createDto(EnvironmentRecipeDto.class)).thenReturn(environmentRecipeDto);
        extendedMachineDto = mock(ExtendedMachineDto.class, new SelfReturningAnswer());
        when(dtoFactory.createDto(ExtendedMachineDto.class)).thenReturn(extendedMachineDto);

        when(wsComponentProvider.get()).thenReturn(workspaceComponent);

        when(recipeServiceClient.getAllRecipes()).thenReturn(recipesPromise);
        when(view.getWorkspaceName()).thenReturn("test");
        when(view.getRecipeUrl()).thenReturn("recipe");
    }

    @Test
    public void delegateShouldBeSet() {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void dialogShouldBeShown() {
        presenter.show(Collections.singletonList(usersWorkspaceDto), componentCallback);

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
        when(workspaceConfigDto.getName()).thenReturn("test");

        presenter.show(Collections.singletonList(usersWorkspaceDto), componentCallback);
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
        List<RecipeDescriptor> recipes = Collections.singletonList(recipeDescriptor);

        callSearchRecipesApplyMethod(recipes);

        verify(popupCallBack, never()).hidePopup();
        verify(view).showFoundByTagRecipes(recipes);
        verify(view).setVisibleTagsError(false);
    }

    private void callSearchRecipesApplyMethod(List<RecipeDescriptor> recipes) throws Exception {
        List<String> tags = Collections.singletonList("test1 test2");

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
        when(userWsPromise.then(Matchers.<Operation<WorkspaceDto>>anyObject())).thenReturn(userWsPromise);
        when(userWsPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(userWsPromise);
        when(recipeServiceClient.getAllRecipes()).thenReturn(recipesPromise);

        presenter.show(Collections.singletonList(usersWorkspaceDto), componentCallback);

        presenter.onCreateButtonClicked();

        verify(recipeServiceClient).getAllRecipes();
        verify(recipesPromise).then(Matchers.<Operation<List<RecipeDescriptor>>>anyObject());

        verify(view).show();
    }

    @Test
    public void workspaceConfigShouldBeGot() {
        when(view.getWorkspaceName()).thenReturn("name");
        when(view.getRecipeUrl()).thenReturn("test");

        clickOnCreateButton();

        verify(view, times(2)).getWorkspaceName();

        verify(dtoFactory).createDto(EnvironmentDto.class);
    }

    @Test
    public void workspaceShouldBeCreatedForDevMachine() throws Exception {
        when(machineConfigDto.isDev()).thenReturn(true);

        callApplyCreateWorkspaceMethod();

        verify(wsComponentProvider).get();
        verify(workspaceComponent).startWorkspace(usersWorkspaceDto, componentCallback);
    }

    private void callApplyCreateWorkspaceMethod() throws Exception {
        Map<String, EnvironmentDto> environments = new HashMap<>();
        environments.put("name", environmentDto);

        when(workspaceConfigDto.getDefaultEnv()).thenReturn("name");
        when(workspaceConfigDto.getEnvironments()).thenReturn(environments);

        clickOnCreateButton();

        verify(userWsPromise).then(workspaceOperation.capture());
        workspaceOperation.getValue().apply(usersWorkspaceDto);
    }

    @Test
    public void workspaceShouldBeCreatedForNotDevMachine() throws Exception {
        when(machineConfigDto.isDev()).thenReturn(false);

        callApplyCreateWorkspaceMethod();

        verify(workspaceComponent).startWorkspace(usersWorkspaceDto, componentCallback);
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
