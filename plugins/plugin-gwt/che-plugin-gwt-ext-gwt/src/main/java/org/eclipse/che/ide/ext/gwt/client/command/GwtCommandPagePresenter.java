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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandPage;

/**
 * Page allows to customize command of {@link GwtCommandType}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandPagePresenter implements GwtCommandPageView.ActionDelegate, CommandPage {

  private final GwtCommandPageView view;

  private CommandImpl editedCommand;
  private GwtCommandModel editedCommandModel;

  // initial value of the 'working directory' parameter
  private String workingDirectoryInitial;
  // initial value of the 'GWT module' parameter
  private String gwtModuleInitial;
  // initial value of the 'Code Server address' parameter
  private String codeServerAddressInitial;

  private DirtyStateListener listener;

  @Inject
  public GwtCommandPagePresenter(GwtCommandPageView view) {
    this.view = view;

    view.setDelegate(this);
  }

  @Override
  public void resetFrom(CommandImpl command) {
    editedCommand = command;

    editedCommandModel = GwtCommandModel.fromCommandLine(command.getCommandLine());

    workingDirectoryInitial = editedCommandModel.getWorkingDirectory();
    gwtModuleInitial = editedCommandModel.getGwtModule();
    codeServerAddressInitial = editedCommandModel.getCodeServerAddress();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setWorkingDirectory(editedCommandModel.getWorkingDirectory());
    view.setGwtModule(editedCommandModel.getGwtModule());
    view.setCodeServerAddress(editedCommandModel.getCodeServerAddress());
  }

  @Override
  public void onSave() {
    workingDirectoryInitial = editedCommandModel.getWorkingDirectory();
    gwtModuleInitial = editedCommandModel.getGwtModule();
    codeServerAddressInitial = editedCommandModel.getCodeServerAddress();
  }

  @Override
  public boolean isDirty() {
    return !(workingDirectoryInitial.equals(editedCommandModel.getWorkingDirectory())
        && gwtModuleInitial.equals(editedCommandModel.getGwtModule())
        && codeServerAddressInitial.equals(editedCommandModel.getCodeServerAddress()));
  }

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {}

  @Override
  public void onWorkingDirectoryChanged() {
    editedCommandModel.setWorkingDirectory(view.getWorkingDirectory());

    editedCommand.setCommandLine(editedCommandModel.toCommandLine());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onGwtModuleChanged() {
    editedCommandModel.setGwtModule(view.getGwtModule());

    editedCommand.setCommandLine(editedCommandModel.toCommandLine());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onCodeServerAddressChanged() {
    editedCommandModel.setCodeServerAddress(view.getCodeServerAddress());

    editedCommand.setCommandLine(editedCommandModel.toCommandLine());
    listener.onDirtyStateChanged();
  }
}
