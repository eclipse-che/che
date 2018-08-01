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
package org.eclipse.che.ide.ext.git.client.importer.page;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.TextBox;

/** @author Roman Nikitenko */
public class GitImporterPageViewImpl extends Composite implements GitImporterPageView {

  @UiField(provided = true)
  Style style;

  @UiField Label labelUrlError;

  @UiField TextBox projectName;

  @UiField TextArea projectDescription;

  @UiField TextBox projectUrl;

  @UiField CheckBox recursive;

  @UiField FlowPanel importerPanel;

  @UiField CheckBox keepDirectory;

  @UiField TextBox directoryName;

  @UiField CheckBox branchSelection;

  @UiField TextBox branch;

  private ActionDelegate delegate;

  @Inject
  public GitImporterPageViewImpl(GitResources resources, GitImporterPageViewImplUiBinder uiBinder) {
    style = resources.gitImporterPageStyle();
    style.ensureInjected();
    initWidget(uiBinder.createAndBindUi(this));

    projectName.getElement().setAttribute("maxlength", "32");
    projectDescription.getElement().setAttribute("maxlength", "256");
  }

  @UiHandler("projectName")
  void onProjectNameChanged(KeyUpEvent event) {
    String projectNameValue = projectName.getValue();

    if (projectNameValue != null && projectNameValue.contains(" ")) {
      projectNameValue = projectNameValue.replace(" ", "-");
      projectName.setValue(projectNameValue);
    }

    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      return;
    }

    delegate.projectNameChanged(projectName.getValue());
  }

  @UiHandler("projectUrl")
  void onProjectUrlChanged(KeyUpEvent event) {
    delegate.projectUrlChanged(projectUrl.getValue());
  }

  @UiHandler("recursive")
  void recursiveHandler(ValueChangeEvent<Boolean> event) {
    delegate.onRecursiveSelected(event.getValue());
  }

  @UiHandler("projectDescription")
  void onProjectDescriptionChanged(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      return;
    }
    delegate.projectDescriptionChanged(projectDescription.getValue());
  }

  @UiHandler({"keepDirectory"})
  void keepDirectoryHandler(ValueChangeEvent<Boolean> event) {
    delegate.keepDirectorySelected(event.getValue());
  }

  @UiHandler({"branchSelection"})
  void branchSelectedHandler(ValueChangeEvent<Boolean> event) {
    delegate.branchSelected(event.getValue());
  }

  @UiHandler("directoryName")
  void onDirectoryNameChanged(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      return;
    }

    delegate.keepDirectoryNameChanged(directoryName.getValue());
  }

  @UiHandler("branch")
  void onBranchNameChanged(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      return;
    }

    delegate.branchNameChanged(branch.getValue());
  }

  @Override
  public void setProjectUrl(@NotNull String url) {
    projectUrl.setText(url);
    delegate.projectUrlChanged(url);
  }

  @Override
  public void markURLValid() {
    projectUrl.markValid();
  }

  @Override
  public void markURLInvalid() {
    projectUrl.markInvalid();
  }

  @Override
  public void unmarkURL() {
    projectUrl.unmark();
  }

  @Override
  public void setURLErrorMessage(@NotNull String message) {
    labelUrlError.setText(message != null ? message : "");
  }

  @Override
  public void markNameValid() {
    projectName.markValid();
  }

  @Override
  public void markNameInvalid() {
    projectName.markInvalid();
  }

  @Override
  public void unmarkName() {
    projectName.unmark();
  }

  @NotNull
  @Override
  public String getProjectName() {
    return projectName.getValue();
  }

  @Override
  public void setProjectName(@NotNull String projectName) {
    this.projectName.setValue(projectName);
    delegate.projectNameChanged(projectName);
  }

  @Override
  public void focusInUrlInput() {
    projectUrl.setFocus(true);
  }

  @Override
  public void setInputsEnableState(boolean isEnabled) {
    projectName.setEnabled(isEnabled);
    projectDescription.setEnabled(isEnabled);
    projectUrl.setEnabled(isEnabled);

    if (isEnabled) {
      focusInUrlInput();
    }
  }

  @Override
  public void setProjectDescription(@NotNull String projectDescription) {
    this.projectDescription.setValue(projectDescription);
  }

  @Override
  public boolean keepDirectory() {
    return keepDirectory.getValue();
  }

  @Override
  public boolean isBranchName() {
    return branchSelection.getValue();
  }

  @Override
  public void setKeepDirectoryChecked(boolean checked) {
    keepDirectory.setValue(checked);
  }

  @Override
  public void setBranchChecked(boolean checked) {
    branchSelection.setValue(checked);
  }

  @Override
  public String getDirectoryName() {
    return directoryName.getValue();
  }

  @Override
  public void setDirectoryName(String directoryName) {
    this.directoryName.setValue(directoryName);
  }

  @Override
  public void enableDirectoryNameField(boolean enable) {
    directoryName.setEnabled(enable);
  }

  @Override
  public void setBranchName(String branchName) {
    branch.setValue(branchName);
  }

  @Override
  public void enableBranchNameField(boolean enable) {
    branch.setEnabled(enable);
  }

  @Override
  public void focusBranchNameField() {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                branch.setFocus(true);
                branch.selectAll();
              }
            });
  }

  @Override
  public String getBranchName() {
    return branch.getValue();
  }

  @Override
  public void highlightDirectoryNameField(boolean highlight) {
    if (highlight) {
      directoryName.addStyleName(style.inputError());
    } else {
      directoryName.removeStyleName(style.inputError());
    }
  }

  @Override
  public void focusDirectoryNameField() {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                directoryName.setFocus(true);
                directoryName.selectAll();
              }
            });
  }

  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface GitImporterPageViewImplUiBinder
      extends UiBinder<DockLayoutPanel, GitImporterPageViewImpl> {}

  public interface Style extends CssResource {
    String mainPanel();

    String namePanel();

    String labelPosition();

    String alignRight();

    String alignLeft();

    String labelErrorPosition();

    String description();

    String label();

    String horizontalLine();

    String inputField();

    String inputError();
  }
}
