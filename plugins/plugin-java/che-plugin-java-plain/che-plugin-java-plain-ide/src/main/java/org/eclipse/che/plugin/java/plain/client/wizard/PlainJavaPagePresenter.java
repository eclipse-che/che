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
package org.eclipse.che.plugin.java.plain.client.wizard;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.java.plain.client.wizard.selector.SelectNodePresenter;
import org.eclipse.che.plugin.java.plain.client.wizard.selector.SelectionDelegate;

/**
 * Presenter of the wizard page which configures Plain Java project.
 *
 * @author Valeriy Svydenko
 */
@Singleton
class PlainJavaPagePresenter extends AbstractWizardPage<MutableProjectConfig>
    implements PlainJavaPageView.ActionDelegate, SelectionDelegate {
  private static final String ATTRIBUTE_VALUE_SEPARATOR = ",   ";

  private final PlainJavaPageView view;
  private final SelectNodePresenter selectNodePresenter;

  private boolean isSourceSelected;

  @Inject
  public PlainJavaPagePresenter(PlainJavaPageView view, SelectNodePresenter selectNodePresenter) {
    super();
    this.view = view;
    this.selectNodePresenter = selectNodePresenter;
    view.setDelegate(this);
  }

  @Override
  public void init(MutableProjectConfig dataObject) {
    super.init(dataObject);

    final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    if (CREATE == wizardMode) {
      setAttribute(SOURCE_FOLDER, Collections.singletonList(DEFAULT_SOURCE_FOLDER_VALUE));
    }
  }

  @Override
  public boolean isCompleted() {
    return true;
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
  }

  @Override
  public void onCoordinatesChanged() {
    setAttribute(
        SOURCE_FOLDER, Arrays.asList(view.getSourceFolder().split(ATTRIBUTE_VALUE_SEPARATOR)));
    setAttribute(
        LIBRARY_FOLDER, Arrays.asList(view.getLibraryFolder().split(ATTRIBUTE_VALUE_SEPARATOR)));

    updateDelegate.updateControls();
  }

  @Override
  public void onBrowseSourceButtonClicked() {
    isSourceSelected = true;
    selectNodePresenter.show(this, dataObject.getName());
  }

  @Override
  public void onBrowseLibraryButtonClicked() {
    isSourceSelected = false;
    selectNodePresenter.show(this, dataObject.getName());
  }

  @Override
  public void onNodeSelected(List<Node> nodes) {
    String projectName = dataObject.getName();

    List<String> nodeRelativePath = new LinkedList<>();

    for (Node node : nodes) {
      Path nodeLocation = ((ResourceNode) node).getData().getLocation();
      nodeRelativePath.add(nodeLocation.makeRelativeTo(valueOf(projectName)).toString());
    }

    if (isSourceSelected) {
      view.setSourceFolder(convertAttributeValuesToString(nodeRelativePath));
    } else {
      view.setLibraryFolder(convertAttributeValuesToString(nodeRelativePath));
    }

    onCoordinatesChanged();
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

  private void updateView() {
    ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
    boolean isCreateWizard = wizardMode == CREATE;

    view.changeBrowseBtnVisibleState(!isCreateWizard);
    view.changeSourceFolderFieldState(isCreateWizard);
    view.changeLibraryPanelVisibleState(!isCreateWizard);

    Map<String, List<String>> attributes = dataObject.getAttributes();

    view.setSourceFolder(
        attributes.get(SOURCE_FOLDER) == null
            ? ""
            : convertAttributeValuesToString(getAttribute(SOURCE_FOLDER)));
    view.setLibraryFolder(convertAttributeValuesToString(getAttribute(LIBRARY_FOLDER)));
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
}
