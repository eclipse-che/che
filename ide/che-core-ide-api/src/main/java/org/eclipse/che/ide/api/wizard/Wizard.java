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
package org.eclipse.che.ide.api.wizard;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The main responsibility of a {@link Wizard} subclass is doing the real work when the wizard
 * completes.
 *
 * @param <T> the type of the data-object that stores collected data
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
public interface Wizard<T> {
  /**
   * Performs some actions required for flipping to first page and returning to the first page of a
   * wizard.
   *
   * @return first page
   */
  @Nullable
  WizardPage<T> navigateToFirst();

  /**
   * Provides a way to move to the next wizard page.
   *
   * @return the next page or {@code null} if wizard has no next page
   */
  @Nullable
  WizardPage<T> navigateToNext();

  /**
   * Provides a way to move to the previous wizard page.
   *
   * @return the previous page or {@code null} if wizard has no previous page
   */
  @Nullable
  WizardPage<T> navigateToPrevious();

  /**
   * Returns whether the wizard has the next page.
   *
   * @return {@code true} if the wizard has next page, otherwise - {@code false}
   */
  boolean hasNext();

  /**
   * Returns whether the wizard has previous page.
   *
   * @return {@code true} if the wizard has previous page, otherwise - {@code false}
   */
  boolean hasPrevious();

  /**
   * Checks whether the wizard may be completed.
   *
   * @return {@code true} if the wizard could be completed, otherwise - {@code false}
   */
  boolean canComplete();

  /** Complete the wizard. */
  void complete(@NotNull CompleteCallback callback);

  /** Sets update control delegate. */
  void setUpdateDelegate(@NotNull UpdateDelegate delegate);

  /** Usually it is required to enable/disable navigation buttons in wizard view. */
  interface UpdateDelegate {
    /** Called when wizard view should be updated. */
    void updateControls();
  }

  interface CompleteCallback {
    /** Called when wizard completed successfully. */
    void onCompleted();

    /** Called when failure occurred while completing wizard. */
    void onFailure(Throwable e);
  }
}
