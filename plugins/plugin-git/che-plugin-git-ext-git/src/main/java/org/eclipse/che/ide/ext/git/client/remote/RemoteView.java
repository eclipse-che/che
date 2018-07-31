/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.remote;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link RemotePresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface RemoteView extends View<RemoteView.ActionDelegate> {
  /** Needs for delegate some function into Applications view. */
  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Close button. */
    void onCloseClicked();

    /** Performs any actions appropriate in response to the user having pressed the Add button. */
    void onAddClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Delete button.
     */
    void onDeleteClicked();

    /**
     * Performs any action in response to the user having select remote.
     *
     * @param remote selected Remote
     */
    void onRemoteSelected(@NotNull Remote remote);
  }

  /**
   * Sets available remote repositories into special place on the view.
   *
   * @param remotes list of available remote repositories.
   */
  void setRemotes(@NotNull List<Remote> remotes);

  /**
   * Change the enable state of the delete button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableDeleteButton(boolean enabled);

  /**
   * Returns whether the view is shown.
   *
   * @return <code>true</code> if the view is shown, and <code>false</code> otherwise
   */
  boolean isShown();

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
