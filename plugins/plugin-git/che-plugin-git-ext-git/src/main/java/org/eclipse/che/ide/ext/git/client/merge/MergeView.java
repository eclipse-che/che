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
package org.eclipse.che.ide.ext.git.client.merge;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link MergePresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface MergeView extends View<MergeView.ActionDelegate> {

  /** Needs for delegate some function into Merge view. */
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /** Performs any actions appropriate in response to the user having pressed the Merge button. */
    void onMergeClicked();

    /**
     * Performs any action in response to the user having select reference.
     *
     * @param reference selected reference
     */
    void onReferenceSelected(@NotNull Reference reference);
  }

  /**
   * Set local branches.
   *
   * @param references local branches
   */
  void setLocalBranches(@NotNull List<Reference> references);

  /**
   * Set remote branches.
   *
   * @param references remote branches
   */
  void setRemoteBranches(@NotNull List<Reference> references);

  /**
   * Change the enable state of the merge button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableMergeButton(boolean enabled);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
