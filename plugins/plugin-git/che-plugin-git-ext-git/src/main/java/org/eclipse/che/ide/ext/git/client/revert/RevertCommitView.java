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
package org.eclipse.che.ide.ext.git.client.revert;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link org.eclipse.che.ide.ext.git.client.revert.RevertCommitPresenter}
 *
 * @author dbocharo
 */
public interface RevertCommitView extends View<RevertCommitView.ActionDelegate> {

  public interface ActionDelegate {
    void onRevertClicked();

    void onCancelClicked();

    void onRevisionSelected(@NotNull Revision revision);

    void onScrolledToBottom();
  }

  /**
   * Set available revisions.
   *
   * @param revisions git revisions
   */
  void setRevisions(@NotNull List<Revision> revisions);

  /**
   * Change the enable state of the revert button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableRevertButton(boolean enabled);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
