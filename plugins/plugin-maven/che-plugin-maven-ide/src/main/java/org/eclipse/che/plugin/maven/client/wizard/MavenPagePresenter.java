/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.wizard;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_ARTIFACT_ID_OPTION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_GROUP_ID_OPTION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_REPOSITORY_OPTION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_VERSION_OPTION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.maven.client.MavenArchetype;
import org.eclipse.che.plugin.maven.client.MavenExtension;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
public class MavenPagePresenter extends AbstractWizardPage<MutableProjectConfig>
    implements MavenPageView.ActionDelegate {

  private final MavenPageView view;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final MavenLocalizationConstant localization;
  private final NotificationManager notificationManager;
  private final ProjectServiceClient projectService;

  @Inject
  public MavenPagePresenter(
      MavenPageView view,
      DialogFactory dialogFactory,
      AppContext appContext,
      MavenLocalizationConstant localization,
      NotificationManager notificationManager,
      ProjectServiceClient projectService) {
    super();
    this.view = view;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.localization = localization;
    this.notificationManager = notificationManager;
    this.projectService = projectService;
    view.setDelegate(this);
  }

  @Override
  public void init(MutableProjectConfig dataObject) {
    super.init(dataObject);

    final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    if (CREATE == wizardMode) {
      // set default values
      setAttribute(VERSION, DEFAULT_VERSION);
      setAttribute(PACKAGING, DEFAULT_PACKAGING);
      setAttribute(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER);
      setAttribute(TEST_SOURCE_FOLDER, DEFAULT_TEST_SOURCE_FOLDER);
    } else if (UPDATE == wizardMode && getAttribute(ARTIFACT_ID).isEmpty()) {
      estimateAndSetAttributes();
    }
  }

  private void estimateAndSetAttributes() {
    appContext
        .getWorkspaceRoot()
        .getContainer(dataObject.getPath())
        .then(
            new Operation<Optional<Container>>() {
              @Override
              public void apply(Optional<Container> container) throws OperationException {
                if (!container.isPresent()) {
                  return;
                }

                container
                    .get()
                    .estimate(MAVEN_ID)
                    .then(
                        new Operation<SourceEstimation>() {
                          @Override
                          public void apply(SourceEstimation estimation) throws OperationException {
                            if (!estimation.isMatched()) {
                              final String resolution = estimation.getResolution();
                              final String errorMessage =
                                  resolution.isEmpty()
                                      ? localization.mavenPageEstimateErrorMessage()
                                      : resolution;
                              dialogFactory
                                  .createMessageDialog(
                                      localization.mavenPageErrorDialogTitle(), errorMessage, null)
                                  .show();
                              return;
                            }

                            Map<String, List<String>> estimatedAttributes =
                                estimation.getAttributes();
                            List<String> artifactIdValues = estimatedAttributes.get(ARTIFACT_ID);
                            if (artifactIdValues != null && !artifactIdValues.isEmpty()) {
                              setAttribute(ARTIFACT_ID, artifactIdValues.get(0));
                            }

                            List<String> groupIdValues = estimatedAttributes.get(GROUP_ID);
                            List<String> parentGroupIdValues =
                                estimatedAttributes.get(PARENT_GROUP_ID);
                            if (groupIdValues != null && !groupIdValues.isEmpty()) {
                              setAttribute(GROUP_ID, groupIdValues.get(0));
                            } else if (parentGroupIdValues != null
                                && !parentGroupIdValues.isEmpty()) {
                              setAttribute(GROUP_ID, parentGroupIdValues.get(0));
                            }

                            List<String> versionValues = estimatedAttributes.get(VERSION);
                            List<String> parentVersionValues =
                                estimatedAttributes.get(PARENT_VERSION);
                            if (versionValues != null && !versionValues.isEmpty()) {
                              setAttribute(VERSION, versionValues.get(0));
                            } else if (parentVersionValues != null
                                && !parentVersionValues.isEmpty()) {
                              setAttribute(VERSION, parentVersionValues.get(0));
                            }

                            List<String> packagingValues = estimatedAttributes.get(PACKAGING);
                            if (packagingValues != null && !packagingValues.isEmpty()) {
                              setAttribute(PACKAGING, packagingValues.get(0));
                            }

                            updateDelegate.updateControls();
                          }
                        })
                    .catchError(
                        new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                            dialogFactory
                                .createMessageDialog(
                                    localization.mavenPageErrorDialogTitle(),
                                    arg.getMessage(),
                                    null)
                                .show();
                            Log.error(MavenPagePresenter.class, arg);
                          }
                        });
              }
            });
  }

  @Override
  public boolean isCompleted() {
    return isCoordinatesCompleted();
  }

  private boolean isCoordinatesCompleted() {
    final String artifactId = getAttribute(ARTIFACT_ID);
    final String groupId = getAttribute(GROUP_ID);
    final String version = getAttribute(VERSION);

    return !(artifactId.isEmpty() || groupId.isEmpty() || version.isEmpty());
  }

  @Override
  public void go(AcceptsOneWidget container) {

    final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    final String projectName = dataObject.getName();

    // use project name for artifactId and groupId for new project
    if (CREATE == wizardMode && projectName != null) {
      if (getAttribute(ARTIFACT_ID).isEmpty()) {
        setAttribute(ARTIFACT_ID, projectName);
      }
      if (getAttribute(GROUP_ID).isEmpty()) {
        setAttribute(GROUP_ID, projectName);
      }
      updateDelegate.updateControls();
    }
    if (CREATE == wizardMode) {
      projectService
          .getItem(Path.valueOf(dataObject.getPath()).parent().append("pom.xml"))
          .then(
              result -> {
                notificationManager.notify(
                    localization.mavenPageArchetypeDisabledTitle(),
                    localization.mavenPageArchetypeDisabledMessage(),
                    WARNING,
                    EMERGE_MODE);
                updateView(container, false);
              })
          .catchError(
              error -> {
                updateView(container, true);
              });
    } else {
      updateView(container, false);
    }
  }

  private void updateView(AcceptsOneWidget container, boolean showArchetype) {
    container.setWidget(view);

    updateView();
    validateCoordinates();

    view.setArchetypeSectionVisibility(showArchetype);
    view.enableArchetypes(view.isGenerateFromArchetypeSelected());
  }

  /** Updates view from data-object. */
  private void updateView() {
    Map<String, List<String>> attributes = dataObject.getAttributes();

    final String artifactId = getAttribute(ARTIFACT_ID);
    if (!artifactId.isEmpty()) {
      view.setArtifactId(artifactId);
    }

    if (attributes.get(GROUP_ID) != null) {
      view.setGroupId(getAttribute(GROUP_ID));
    } else {
      view.setGroupId(getAttribute(PARENT_GROUP_ID));
    }

    if (attributes.get(VERSION) != null) {
      view.setVersion(getAttribute(VERSION));
    } else {
      view.setVersion(getAttribute(PARENT_VERSION));
    }

    view.setPackaging(getAttribute(PACKAGING));
  }

  @Override
  public void onCoordinatesChanged() {
    setAttribute(ARTIFACT_ID, view.getArtifactId());
    setAttribute(GROUP_ID, view.getGroupId());
    setAttribute(VERSION, view.getVersion());

    packagingChanged(view.getPackaging());
    validateCoordinates();
    updateDelegate.updateControls();
  }

  @Override
  public void packagingChanged(String packaging) {
    Map<String, List<String>> attributes = dataObject.getAttributes();
    attributes.put(PACKAGING, Arrays.asList(packaging));
    if ("pom".equals(packaging)) {
      attributes.remove(SOURCE_FOLDER);
      attributes.remove(TEST_SOURCE_FOLDER);
    } else {
      attributes.put(SOURCE_FOLDER, Arrays.asList(DEFAULT_SOURCE_FOLDER));
      attributes.put(TEST_SOURCE_FOLDER, Arrays.asList(DEFAULT_TEST_SOURCE_FOLDER));
    }

    updateDelegate.updateControls();
  }

  @Override
  public void generateFromArchetypeChanged(boolean isGenerateFromArchetype) {
    view.setPackagingVisibility(!isGenerateFromArchetype);
    view.enableArchetypes(isGenerateFromArchetype);
    if (!isGenerateFromArchetype) {
      view.clearArchetypes();
    } else {
      view.setArchetypes(MavenExtension.getAvailableArchetypes());
    }
    archetypeChanged(MavenExtension.getAvailableArchetypes().get(0));
    updateDelegate.updateControls();
  }

  @Override
  public void archetypeChanged(MavenArchetype archetype) {
    dataObject.getOptions().put("type", "archetype");
    dataObject.getOptions().put(ARCHETYPE_GROUP_ID_OPTION, archetype.getGroupId());
    dataObject.getOptions().put(ARCHETYPE_ARTIFACT_ID_OPTION, archetype.getArtifactId());
    dataObject.getOptions().put(ARCHETYPE_VERSION_OPTION, archetype.getVersion());
    dataObject.getOptions().put(ARCHETYPE_REPOSITORY_OPTION, archetype.getRepository());
    updateDelegate.updateControls();
  }

  private void validateCoordinates() {
    view.showArtifactIdMissingIndicator(view.getArtifactId().isEmpty());
    view.showGroupIdMissingIndicator(view.getGroupId().isEmpty());
    view.showVersionMissingIndicator(view.getVersion().isEmpty());
  }

  /** Reads single value of attribute from data-object. */
  @NotNull
  private String getAttribute(String attrId) {
    Map<String, List<String>> attributes = dataObject.getAttributes();
    List<String> values = attributes.get(attrId);
    if (values == null || values.isEmpty()) {
      return "";
    }
    return firstNonNull(values.get(0), "");
  }

  /** Sets single value of attribute of data-object. */
  private void setAttribute(String attrId, String value) {
    Map<String, List<String>> attributes = dataObject.getAttributes();
    attributes.put(attrId, singletonList(value));
  }
}
