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
package org.eclipse.che.plugin.pullrequest.client.dialogs.commit;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View for committing uncommitted project changes.
 *
 * @author Kevin Pollet
 */
public interface CommitView extends View<CommitView.ActionDelegate> {
  /** Opens the commit view with the given commit description. */
  void show(String commitDescription);

  /** Close the commit view. */
  void close();

  /**
   * Returns the current commit description.
   *
   * @return the current commit description.
   */
  @NotNull
  String getCommitDescription();

  /**
   * Enables or disables the button OK.
   *
   * @param enabled {@code true} to enable the OK button, {@code false} otherwise.
   */
  void setOkButtonEnabled(final boolean enabled);

  /**
   * Returns if the untracked files must be added.
   *
   * @return {@code true} if untracked files must be added, {@code false} otherwise.
   */
  boolean isIncludeUntracked();

  /** The action delegate. */
  interface ActionDelegate {
    /** Called when project changes must be committed. */
    void onOk();

    /** Called when project changes must not be committed. */
    void onContinue();

    /** Called when the operation must be aborted. */
    void onCancel();

    /** Called when the commit description is changed. */
    void onCommitDescriptionChanged();
  }
}
