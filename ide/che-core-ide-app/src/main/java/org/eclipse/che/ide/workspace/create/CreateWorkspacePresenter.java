/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace.create;

import static java.util.Collections.singletonMap;

import com.google.gwt.core.client.Callback;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.recipe.OldRecipeDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * The class contains business logic which allow to create user workspace if it doesn't exist.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateWorkspacePresenter implements CreateWorkspaceView.ActionDelegate {

  protected static final String MEMORY_LIMIT_BYTES = Long.toString(2000L * 1024L * 1024L);
  static final String RECIPE_TYPE = "docker";
  static final int SKIP_COUNT = 0;
  static final int MAX_COUNT = 100;
  static final int MAX_NAME_LENGTH = 20;
  static final int MIN_NAME_LENGTH = 3;
  private static final RegExp FILE_NAME = RegExp.compile("^[A-Za-z0-9_\\s-\\.]+$");
  private static final String URL_PATTERN =
      "^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$";
  private static final RegExp URL = RegExp.compile(URL_PATTERN);
  private final CreateWorkspaceView view;
  private final DtoFactory dtoFactory;
  private final WorkspaceServiceClient workspaceClient;
  private final CoreLocalizationConstant locale;
  //    private final Provider<DefaultWorkspaceComponent> wsComponentProvider;
  //    private final RecipeServiceClient                 recipeService;
  private final BrowserAddress browserAddress;

  private Callback<Workspace, Exception> callback;
  private List<OldRecipeDescriptor> recipes;
  private List<String> workspacesNames;

  @Inject
  public CreateWorkspacePresenter(
      CreateWorkspaceView view,
      DtoFactory dtoFactory,
      WorkspaceServiceClient workspaceClient,
      CoreLocalizationConstant locale,
      //                                    Provider<DefaultWorkspaceComponent> wsComponentProvider,
      //                                    RecipeServiceClient recipeService,
      BrowserAddress browserAddress) {
    this.view = view;
    this.view.setDelegate(this);

    this.dtoFactory = dtoFactory;
    this.workspaceClient = workspaceClient;
    this.locale = locale;
    //        this.wsComponentProvider = wsComponentProvider;
    //        this.recipeService = recipeService;
    this.browserAddress = browserAddress;

    this.workspacesNames = new ArrayList<>();
  }

  /**
   * Shows special dialog window which allows set up workspace which will be created.
   *
   * @param workspaces list of existing workspaces
   */
  public void show(
      /*List<WorkspaceDto> workspaces, */ final Callback<Workspace, Exception> callback) {
    this.callback = callback;

    workspacesNames.clear();

    //        for (WorkspaceDto workspace : workspaces) {
    //            workspacesNames.add(workspace.getConfig().getName());
    //        }

    //        Promise<List<OldRecipeDescriptor>> recipes = recipeService.getAllRecipes();
    //
    //        recipes.then(new Operation<List<OldRecipeDescriptor>>() {
    //            @Override
    //            public void apply(List<OldRecipeDescriptor> recipeDescriptors) throws OperationException {
    //                CreateWorkspacePresenter.this.recipes = recipeDescriptors;
    //            }
    //        });

    String workspaceName = browserAddress.getWorkspaceName();

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
  }

  /** {@inheritDoc} */
  @Override
  public void onNameChanged() {
    validateCreateWorkspaceForm();
  }

  /** {@inheritDoc} */
  @Override
  public void onCreateButtonClicked() {
    view.hide();

    createWorkspace();
  }

  private void createWorkspace() {
    WorkspaceConfigDto workspaceConfig = getWorkspaceConfig();

    workspaceClient
        .create(workspaceConfig, null)
        .then(
            new Operation<WorkspaceImpl>() {
              @Override
              public void apply(WorkspaceImpl workspace) throws OperationException {
                callback.onSuccess(workspace);
                //                DefaultWorkspaceComponent component = wsComponentProvider.get();
                //                component.startWorkspace(workspace, callback);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new Exception(arg.getCause()));
              }
            });
  }

  private WorkspaceConfigDto getWorkspaceConfig() {
    String wsName = view.getWorkspaceName();

    return dtoFactory
        .createDto(WorkspaceConfigDto.class)
        .withName(wsName)
        .withDefaultEnv(wsName)
        .withEnvironments(singletonMap(wsName, getSingleMachineEnvironment()));
  }

  private EnvironmentDto getMultiMachineEnvironment() {
    RecipeDto recipe =
        dtoFactory
            .createDto(RecipeDto.class)
            .withType("compose")
            .withContentType("application/x-yaml")
            .withContent(
                "services:\n db:\n  image: eclipse/mysql\n  environment:\n   MYSQL_ROOT_PASSWORD: password\n   MYSQL_DATABASE: petclinic\n   MYSQL_USER: petclinic\n   MYSQL_PASSWORD: password\n  mem_limit: 1073741824\n dev-machine:\n  image: eclipse/ubuntu_jdk8\n  mem_limit: 2147483648\n  depends_on:\n    - db");

    List<String> devInstallers = new ArrayList<>();
    devInstallers.add("org.eclipse.che.exec");
    devInstallers.add("org.eclipse.che.terminal");
    devInstallers.add("org.eclipse.che.ws-agent");
    devInstallers.add("org.eclipse.che.ssh");

    MachineConfigDto devMachine =
        dtoFactory
            .createDto(MachineConfigDto.class)
            .withInstallers(devInstallers)
            .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

    List<String> dbInstallers = new ArrayList<>();
    dbInstallers.add("org.eclipse.che.exec");
    dbInstallers.add("org.eclipse.che.terminal");

    MachineConfigDto dbMachine =
        dtoFactory
            .createDto(MachineConfigDto.class)
            .withInstallers(dbInstallers)
            .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

    Map<String, MachineConfigDto> machines = new HashMap<>();
    machines.put("dev-machine", devMachine);
    machines.put("db", dbMachine);

    return dtoFactory.createDto(EnvironmentDto.class).withRecipe(recipe).withMachines(machines);
  }

  private EnvironmentDto getSingleMachineEnvironment() {
    RecipeDto recipe =
        dtoFactory
            .createDto(RecipeDto.class)
            .withType("dockerimage")
            .withLocation("eclipse/ubuntu_jdk8");

    List<String> installers = new ArrayList<>();
    installers.add("org.eclipse.che.exec");
    installers.add("org.eclipse.che.terminal");
    installers.add("org.eclipse.che.ws-agent");
    installers.add("org.eclipse.che.ssh");

    MachineConfigDto machine =
        dtoFactory
            .createDto(MachineConfigDto.class)
            .withInstallers(installers)
            .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

    return dtoFactory
        .createDto(EnvironmentDto.class)
        .withRecipe(recipe)
        .withMachines(singletonMap("default", machine));
  }
}
