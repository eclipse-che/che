/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.ide.project;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.composer.shared.Constants.PACKAGE;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.composer.ide.ComposerLocalizationConstant;

/** @author Kaloyan Raev */
public class ComposerPagePresenter extends AbstractWizardPage<MutableProjectConfig>
    implements ComposerPageView.ActionDelegate {

  private static final String ATTRIBUTE_VALUE_SEPARATOR = ",   ";

  private final ComposerPageView view;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final ComposerLocalizationConstant localization;

  @Inject
  public ComposerPagePresenter(
      ComposerPageView view,
      DialogFactory dialogFactory,
      AppContext appContext,
      ComposerLocalizationConstant localization) {
    super();
    this.view = view;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.localization = localization;
    view.setDelegate(this);
  }

  @Override
  public void init(MutableProjectConfig dataObject) {
    super.init(dataObject);

    final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    if (CREATE == wizardMode) {
      setAttribute(PACKAGE, Collections.<String>emptyList());
    } else if (UPDATE == wizardMode && getAttribute(PACKAGE).isEmpty()) {
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
                    .estimate(COMPOSER_PROJECT_TYPE_ID)
                    .then(
                        new Operation<SourceEstimation>() {
                          @Override
                          public void apply(SourceEstimation estimation) throws OperationException {
                            if (!estimation.isMatched()) {
                              final String resolution = estimation.getResolution();
                              final String errorMessage =
                                  resolution.isEmpty()
                                      ? localization.composerPageEstimateErrorMessage()
                                      : resolution;
                              dialogFactory
                                  .createMessageDialog(
                                      localization.composerPageErrorDialogTitle(),
                                      errorMessage,
                                      null)
                                  .show();
                              return;
                            }

                            Map<String, List<String>> estimatedAttributes =
                                estimation.getAttributes();
                            List<String> artifactIdValues = estimatedAttributes.get(PACKAGE);
                            if (artifactIdValues != null && !artifactIdValues.isEmpty()) {
                              setAttribute(PACKAGE, artifactIdValues);
                            }

                            updateDelegate.updateControls();
                          }
                        });
              }
            });
  }

  @Override
  public boolean isCompleted() {
    return areAttributesCompleted();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    final String projectName = dataObject.getName();

    if (CREATE == wizardMode && projectName != null) {
      updateDelegate.updateControls();
    }

    updateView();
    validateAttributes();
  }

  @Override
  public void onAttributesChanged() {
    setAttribute(PACKAGE, Arrays.asList(view.getPackage()));

    validateAttributes();
    updateDelegate.updateControls();
  }

  private List<String> getAttribute(String attrId) {
    Map<String, List<String>> attributes = dataObject.getAttributes();
    List<String> values = attributes.get(attrId);
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    return values;
  }

  private void setAttribute(String attrId, List<String> value) {
    Map<String, List<String>> attributes = dataObject.getAttributes();
    attributes.put(attrId, value);
  }

  private boolean areAttributesCompleted() {
    return !getAttribute(PACKAGE).isEmpty();
  }

  private void updateView() {
    ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    boolean isCreateWizard = wizardMode == CREATE;

    view.changePackageFieldState(isCreateWizard);

    Map<String, List<String>> attributes = dataObject.getAttributes();

    view.setPackage(
        attributes.get(PACKAGE) == null
            ? ""
            : convertAttributeValuesToString(getAttribute(PACKAGE)));
  }

  private String convertAttributeValuesToString(List<String> values) {
    StringBuilder result = new StringBuilder();
    for (String value : values) {
      result.append(value).append(ATTRIBUTE_VALUE_SEPARATOR);
    }

    return result.toString().isEmpty()
        ? result.toString()
        : result.delete(result.lastIndexOf(ATTRIBUTE_VALUE_SEPARATOR), result.length()).toString();
  }

  private void validateAttributes() {
    view.showPackageMissingIndicator(view.getPackage().isEmpty());
  }
}
