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
package org.eclipse.che.ide.command.editor.page.name;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

/**
 * Presenter for {@link CommandEditorPage} which allows to edit command's name.
 *
 * @author Artem Zatsarynnyi
 */
public class NamePage extends AbstractCommandEditorPage implements NamePageView.ActionDelegate {

  private final NamePageView view;
  private final CommandExecutor commandExecutor;

  /** Initial value of the command's name. */
  private String commandNameInitial;

  @Inject
  public NamePage(NamePageView view, EditorMessages messages, CommandExecutor commandExecutor) {
    super(messages.pageNameTitle());

    this.view = view;
    this.commandExecutor = commandExecutor;

    view.setDelegate(this);
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  protected void initialize() {
    commandNameInitial = editedCommand.getName();

    view.setCommandName(editedCommand.getName());
  }

  @Override
  public boolean isDirty() {
    if (editedCommand == null) {
      return false;
    }

    return !(commandNameInitial.equals(editedCommand.getName()));
  }

  @Override
  public void onNameChanged(String name) {
    editedCommand.setName(name);

    notifyDirtyStateChanged();
  }

  @Override
  public void onCommandRun() {
    commandExecutor.executeCommand(editedCommand);
  }
}
