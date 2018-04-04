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
package org.eclipse.che.ide.command.editor.page;

import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Abstract {@link CommandEditorPage} that provides basic functionality.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractCommandEditorPage implements CommandEditorPage {

  private final String title;

  protected CommandImpl editedCommand;

  private DirtyStateListener listener;

  /** Creates new page with the given title and tooltip. */
  protected AbstractCommandEditorPage(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void edit(CommandImpl command) {
    editedCommand = command;

    initialize();
    notifyDirtyStateChanged();
  }

  /**
   * Called every time when command is opening in the editor. Typically, implementor should do
   * initial setup of the page with the {@link #editedCommand}.
   */
  protected abstract void initialize();

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  /**
   * Should be called by page every time when any command modifications on the page have been
   * performed.
   */
  protected void notifyDirtyStateChanged() {
    if (listener != null) {
      listener.onDirtyStateChanged();
    }
  }
}
