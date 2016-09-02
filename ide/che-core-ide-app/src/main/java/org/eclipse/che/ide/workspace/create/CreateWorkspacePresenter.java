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

import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.workspace.DefaultWorkspaceComponent;
import org.eclipse.che.ide.workspace.create.CreateWorkspaceView.HidePopupCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.machine.shared.Constants.WS_MACHINE_NAME;

/**
 * The class contains business logic which allow to create user workspace if it doesn't exist.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateWorkspacePresenter implements CreateWorkspaceView.ActionDelegate {

    private static final RegExp FILE_NAME   = RegExp.compile("^[A-Za-z0-9_\\s-\\.]+$");
    private static final String URL_PATTERN = "^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$";
    private static final RegExp URL         = RegExp.compile(URL_PATTERN);

    static final String RECIPE_TYPE     = "docker";
    static final int    SKIP_COUNT      = 0;
    static final int    MAX_COUNT       = 100;
    static final int    MAX_NAME_LENGTH = 20;
    static final int    MIN_NAME_LENGTH = 3;

    private final CreateWorkspaceView                 view;
    private final DtoFactory                          dtoFactory;
    private final WorkspaceServiceClient              workspaceClient;
    private final CoreLocalizationConstant            locale;
    private final Provider<DefaultWorkspaceComponent> wsComponentProvider;
    private final RecipeServiceClient                 recipeService;
    private final BrowserQueryFieldRenderer           browserQueryFieldRenderer;

    private Callback<Component, Exception> callback;
    private List<RecipeDescriptor>         recipes;
    private List<String>                   workspacesNames;

    @Inject
    public CreateWorkspacePresenter(CreateWorkspaceView view,
                                    DtoFactory dtoFactory,
                                    WorkspaceServiceClient workspaceClient,
                                    CoreLocalizationConstant locale,
                                    Provider<DefaultWorkspaceComponent> wsComponentProvider,
                                    RecipeServiceClient recipeService,
                                    BrowserQueryFieldRenderer browserQueryFieldRenderer) {
        this.view = view;
        this.view.setDelegate(this);

        this.dtoFactory = dtoFactory;
        this.workspaceClient = workspaceClient;
        this.locale = locale;
        this.wsComponentProvider = wsComponentProvider;
        this.recipeService = recipeService;
        this.browserQueryFieldRenderer = browserQueryFieldRenderer;

        this.workspacesNames = new ArrayList<>();
    }

    /**
     * Shows special dialog window which allows set up workspace which will be created.
     *
     * @param workspaces
     *         list of existing workspaces
     */
    public void show(List<WorkspaceDto> workspaces, final Callback<Component, Exception> callback) {
        this.callback = callback;

        workspacesNames.clear();

        for (WorkspaceDto workspace : workspaces) {
            workspacesNames.add(workspace.getConfig().getName());
        }

        Promise<List<RecipeDescriptor>> recipes = recipeService.getAllRecipes();

        recipes.then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> recipeDescriptors) throws OperationException {
                CreateWorkspacePresenter.this.recipes = recipeDescriptors;
            }
        });

        String workspaceName = browserQueryFieldRenderer.getWorkspaceName();

        view.setWorkspaceName(workspaceName);

        validateCreateWorkspaceForm();

        view.show();
    }

    private void validateCreateWorkspaceForm() {
        String workspaceName = view.getWorkspaceName();

        int nameLength = workspaceName.length();

        String errorDescription = "";

        boolean nameLengthIsInCorrect = nameLength < MIN_NAME_LENGTH || nameLength > MAX_NAME_LENGTH;

        if (nameLengthIsInCorrect) {
            errorDescription = locale.createWsNameLengthIsNotCorrect();
        }

        boolean nameIsInCorrect = !FILE_NAME.test(workspaceName);

        if (nameIsInCorrect) {
            errorDescription = locale.createWsNameIsNotCorrect();
        }

        boolean nameAlreadyExist = workspacesNames.contains(workspaceName);

        if (nameAlreadyExist) {
            errorDescription = locale.createWsNameAlreadyExist();
        }

        view.showValidationNameError(errorDescription);

        String recipeUrl = view.getRecipeUrl();

        boolean urlIsIncorrect = !Strings.isNullOrEmpty(recipeUrl) && !URL.test(recipeUrl) ;

        view.setVisibleUrlError(urlIsIncorrect);

        view.setEnableCreateButton(!urlIsIncorrect && errorDescription.isEmpty());
    }

    /** {@inheritDoc} */
    @Override
    public void onNameChanged() {
        validateCreateWorkspaceForm();
    }

    /** {@inheritDoc} */
    @Override
    public void onRecipeUrlChanged() {
        validateCreateWorkspaceForm();
    }

    /** {@inheritDoc} */
    @Override
    public void onTagsChanged(final HidePopupCallBack callBack) {
        recipeService.searchRecipes(view.getTags(), RECIPE_TYPE, SKIP_COUNT, MAX_COUNT).then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> recipes) throws OperationException {
                boolean isRecipesEmpty = recipes.isEmpty();

                if (isRecipesEmpty) {
                    callBack.hidePopup();
                } else {
                    view.showFoundByTagRecipes(recipes);
                }

                view.setVisibleTagsError(isRecipesEmpty);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onPredefinedRecipesClicked() {
        view.showPredefinedRecipes(recipes);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateButtonClicked() {
        view.hide();

        createWorkspace();
    }

    private void createWorkspace() {
        WorkspaceConfigDto workspaceConfig = getWorkspaceConfig();

        workspaceClient.create(workspaceConfig, null).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                DefaultWorkspaceComponent component = wsComponentProvider.get();
                component.startWorkspace(workspace, callback);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new Exception(arg.getCause()));
            }
        });
    }

    private WorkspaceConfigDto getWorkspaceConfig() {
        String wsName = view.getWorkspaceName();

        EnvironmentRecipeDto recipe = dtoFactory.createDto(EnvironmentRecipeDto.class)
                                                .withType("compose")
                                                .withLocation(view.getRecipeUrl())
                                                .withContentType("application/x-yaml");

        ExtendedMachineDto machine = dtoFactory.createDto(ExtendedMachineDto.class)
                                               .withAgents(Collections.singletonList("ws-agent"));

        EnvironmentDto environment = dtoFactory.createDto(EnvironmentDto.class)
                                               .withRecipe(recipe)
                                               .withMachines(singletonMap(WS_MACHINE_NAME, machine));

        return dtoFactory.createDto(WorkspaceConfigDto.class)
                         .withName(wsName)
                         .withDefaultEnv(wsName)
                         .withEnvironments(singletonMap(wsName, environment));
    }
}
