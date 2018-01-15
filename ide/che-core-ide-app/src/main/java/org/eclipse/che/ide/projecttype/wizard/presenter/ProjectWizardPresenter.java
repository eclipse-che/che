/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projecttype.wizard.presenter;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.IMPORT;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.dto.AttributeDto;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.NewProjectConfigImpl;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizard;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardFactory;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardRegistry;
import org.eclipse.che.ide.projecttype.wizard.categoriespage.CategoriesPagePresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for project wizard.
 *
 * @author Evgen Vidolob
 * @author Oleksii Orel
 * @author Sergii Leschenko
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectWizardPresenter
    implements Wizard.UpdateDelegate,
        ProjectWizardView.ActionDelegate,
        CategoriesPagePresenter.ProjectTypeSelectionListener,
        CategoriesPagePresenter.ProjectTemplateSelectionListener {

  private final ProjectWizardView view;
  private final ProjectWizardFactory projectWizardFactory;
  private final ProjectWizardRegistry wizardRegistry;
  private final Provider<CategoriesPagePresenter> categoriesPageProvider;
  private final DialogFactory dialogFactory;
  private final Map<ProjectTypeDto, ProjectWizard> wizardsCache;
  private CategoriesPagePresenter categoriesPage;
  private ProjectWizard wizard;
  private ProjectWizard importWizard;
  private WizardPage currentPage;

  private ProjectWizardMode wizardMode;

  @Inject
  public ProjectWizardPresenter(
      ProjectWizardView view,
      ProjectWizardFactory projectWizardFactory,
      ProjectWizardRegistry wizardRegistry,
      Provider<CategoriesPagePresenter> categoriesPageProvider,
      DialogFactory dialogFactory) {
    this.view = view;
    this.projectWizardFactory = projectWizardFactory;
    this.wizardRegistry = wizardRegistry;
    this.categoriesPageProvider = categoriesPageProvider;
    this.dialogFactory = dialogFactory;
    wizardsCache = new HashMap<>();
    view.setDelegate(this);
  }

  @Override
  public void onBackClicked() {
    final WizardPage prevPage = wizard.navigateToPrevious();
    if (prevPage != null) {
      showPage(prevPage);
    }
  }

  @Override
  public void onNextClicked() {
    final WizardPage nextPage = wizard.navigateToNext();
    if (nextPage != null) {
      showPage(nextPage);
    }
  }

  @Override
  public void onSaveClicked() {
    view.setLoaderVisibility(true);
    wizard.complete(
        new Wizard.CompleteCallback() {
          @Override
          public void onCompleted() {
            view.close();
          }

          @Override
          public void onFailure(Throwable e) {
            dialogFactory.createMessageDialog("Error", e.getMessage(), null).show();
            view.setLoaderVisibility(false);
          }
        });
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void updateControls() {
    view.setPreviousButtonEnabled(wizard.hasPrevious());
    view.setNextButtonEnabled(wizard.hasNext() && currentPage.isCompleted());
    view.setFinishButtonEnabled(wizard.canComplete());
  }

  /** Open the project wizard for creating a new project. */
  public void show() {
    show(Path.ROOT);
  }

  /** Open the project wizard with given mode. */
  public void show(Path parent) {
    resetState();
    this.wizardMode = CREATE;
    MutableProjectConfig config = new MutableProjectConfig();
    config.setPath(parent.toString());
    showDialog(config);
  }

  /** Open the project wizard for updating the given {@code project}. */
  public void show(@NotNull MutableProjectConfig project) {
    resetState();
    wizardMode = UPDATE;
    showDialog(project);
  }

  private void resetState() {
    wizardsCache.clear();
    categoriesPage = categoriesPageProvider.get();
    wizardMode = null;
    categoriesPage.setProjectTypeSelectionListener(this);
    categoriesPage.setProjectTemplateSelectionListener(this);
    importWizard = null;
  }

  private void showDialog(@Nullable MutableProjectConfig dataObject) {
    wizard = createDefaultWizard(dataObject, wizardMode);
    final WizardPage<MutableProjectConfig> firstPage = wizard.navigateToFirst();
    if (firstPage != null) {
      showPage(firstPage);
      view.showDialog(wizardMode);
    }
  }

  @Override
  public void onProjectTypeSelected(ProjectTypeDto projectType) {
    final MutableProjectConfig prevData = wizard.getDataObject();
    wizard = getWizardForProjectType(projectType, prevData);
    wizard.navigateToFirst();
    final MutableProjectConfig newProject = wizard.getDataObject();

    // some values should be shared between wizards for different project types
    newProject.setPath(prevData.getPath());
    newProject.setName(prevData.getName());
    newProject.setDescription(prevData.getDescription());
    newProject.setMixins(prevData.getMixins());
    if (wizardMode == UPDATE) {
      newProject.setAttributes(prevData.getAttributes());
    } else {
      final MutableProjectConfig.MutableSourceStorage sourceStorage = prevData.getSource();
      if (sourceStorage
          != null) { // some values should be cleared when user switch between categories
        sourceStorage.setLocation("");
        sourceStorage.setType("");
        sourceStorage.getParameters().clear();
      }
      prevData.getProjects().clear();

      final List<AttributeDto> attributes = projectType.getAttributes();
      Map<String, List<String>> prevDataAttributes = prevData.getAttributes();
      Map<String, List<String>> newAttributes = new HashMap<>();
      for (AttributeDto attribute : attributes) {
        if (prevDataAttributes.containsKey(attribute.getName())) {
          newAttributes.put(attribute.getName(), prevDataAttributes.get(attribute.getName()));
        }
      }
      newProject.setAttributes(newAttributes);
    }

    // set dataObject's values from projectType
    newProject.setType(projectType.getId());
  }

  @Override
  public void onProjectTemplateSelected(ProjectTemplateDescriptor projectTemplate) {
    final MutableProjectConfig dataObject = wizard.getDataObject();
    wizard =
        importWizard == null
            ? importWizard = createDefaultWizard(dataObject, IMPORT)
            : importWizard;
    wizard.navigateToFirst();

    // set dataObject's values from projectTemplate
    final NewProjectConfig newProjectConfig = new NewProjectConfigImpl(projectTemplate);
    dataObject.setType(newProjectConfig.getType());
    dataObject.setSource(newProjectConfig.getSource());
    dataObject.setAttributes(newProjectConfig.getAttributes());
    dataObject.setOptions(newProjectConfig.getOptions());
    dataObject.setCommands(projectTemplate.getCommands());
  }

  /** Creates or returns project wizard for the specified projectType with the given dataObject. */
  private ProjectWizard getWizardForProjectType(
      @NotNull ProjectTypeDto projectType, @NotNull MutableProjectConfig configDto) {
    if (wizardsCache.containsKey(projectType)) {
      return wizardsCache.get(projectType);
    }

    final Optional<ProjectWizardRegistrar> wizardRegistrar =
        wizardRegistry.getWizardRegistrar(projectType.getId());
    if (!wizardRegistrar.isPresent()) {
      // should never occur
      throw new IllegalStateException(
          "WizardRegistrar for the project type " + projectType.getId() + " isn't registered.");
    }

    List<Provider<? extends WizardPage<MutableProjectConfig>>> pageProviders =
        wizardRegistrar.get().getWizardPages();
    final ProjectWizard projectWizard = createDefaultWizard(configDto, wizardMode);
    for (Provider<? extends WizardPage<MutableProjectConfig>> provider : pageProviders) {
      projectWizard.addPage(provider.get(), 1, false);
    }

    wizardsCache.put(projectType, projectWizard);
    return projectWizard;
  }

  /** Creates and returns 'default' project wizard with pre-defined pages only. */
  private ProjectWizard createDefaultWizard(
      @Nullable MutableProjectConfig dataObject, @NotNull ProjectWizardMode mode) {
    final ProjectWizard projectWizard = projectWizardFactory.newWizard(dataObject, mode);
    projectWizard.setUpdateDelegate(this);

    // add pre-defined pages - first and last
    projectWizard.addPage(categoriesPage);
    return projectWizard;
  }

  private void showPage(@NotNull WizardPage wizardPage) {
    currentPage = wizardPage;
    updateControls();
    view.showPage(currentPage);
  }
}
