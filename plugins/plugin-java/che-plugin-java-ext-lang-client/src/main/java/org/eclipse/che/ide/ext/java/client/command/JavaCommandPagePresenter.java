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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.command.mainclass.SelectNodePresenter;
import org.eclipse.che.ide.resource.Path;

/**
 * Page allows to customize command of {@link JavaCommandType}.
 *
 * @author Valeriy Svydenko
 * @author Artem Zatsarynnyi
 */
@Singleton
public class JavaCommandPagePresenter implements JavaCommandPageView.ActionDelegate, CommandPage {

  private final JavaCommandPageView view;
  private final SelectNodePresenter selectNodePresenter;

  private CommandImpl editedCommand;
  private JavaCommandModel editedCommandModel;

  // initial value of the 'Main class' parameter
  private String mainClassInitial;
  // initial value of the command line
  private String commandLineInitial;

  private DirtyStateListener listener;
  private FieldStateActionDelegate delegate;

  @Inject
  public JavaCommandPagePresenter(
      JavaCommandPageView view, SelectNodePresenter selectNodePresenter) {
    this.view = view;
    this.selectNodePresenter = selectNodePresenter;
    view.setDelegate(this);
  }

  @Override
  public void resetFrom(CommandImpl command) {
    editedCommand = command;

    editedCommandModel = JavaCommandModel.fromCommandLine(command.getCommandLine());

    mainClassInitial = editedCommandModel.getMainClass();
    commandLineInitial = editedCommandModel.getCommandLine();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setMainClass(editedCommandModel.getMainClass());
    view.setCommandLine(editedCommandModel.getCommandLine());

    delegate.updatePreviewURLState(false);
  }

  @Override
  public void onSave() {
    mainClassInitial = editedCommandModel.getMainClass();
    commandLineInitial = editedCommandModel.getCommandLine();
  }

  @Override
  public boolean isDirty() {
    return !commandLineInitial.equals(editedCommandModel.getCommandLine())
        || !mainClassInitial.equals(editedCommandModel.getMainClass());
  }

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onAddMainClassBtnClicked() {
    selectNodePresenter.show(this);
  }

  @Override
  public void onCommandLineChanged() {
    editedCommandModel.setCommandLine(view.getCommandLine());

    editedCommand.setCommandLine(editedCommandModel.toCommandLine());
    listener.onDirtyStateChanged();
  }

  public void setMainClass(Resource resource, String fqn) {
    if (editedCommandModel.getMainClass().equals(resource.getLocation().toString())) {
      return;
    }

    final Optional<Project> project = resource.getRelatedProject();

    if (!project.isPresent()) {
      return;
    }

    final Path relPath =
        resource.getLocation().removeFirstSegments(project.get().getLocation().segmentCount());

    view.setMainClass(relPath.toString());

    String commandLine = editedCommandModel.getCommandLine();
    commandLine = commandLine.replace(editedCommandModel.getMainClass(), relPath.toString());
    commandLine = commandLine.replace(' ' + editedCommandModel.getMainClassFQN(), ' ' + fqn);

    editedCommandModel.setMainClass(view.getMainClass());
    editedCommandModel.setCommandLine(commandLine);

    editedCommand.setCommandLine(editedCommandModel.toCommandLine());
    listener.onDirtyStateChanged();
  }

  public JavaCommandModel getEditedCommandModel() {
    return editedCommandModel;
  }
}
