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
package org.eclipse.che.ide.api.wizard;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * The main responsibility of a {@link WizardPage} subclass is collecting data.
 *
 * @param <T> the type of the data-object that stores collected data
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
public interface WizardPage<T> extends Presenter {
  /** Initializes page by the passed {@code dataObject}. */
  void init(T dataObject);

  void setContext(@NotNull Map<String, String> context);

  /** Sets update control delegate. */
  void setUpdateDelegate(@NotNull Wizard.UpdateDelegate delegate);

  /**
   * Returns whether this page is completed or not. This information is typically used by the wizard
   * to decide when it is okay to finish.
   *
   * @return {@code true} if this page is completed, otherwise - {@code false}
   */
  boolean isCompleted();

  /**
   * Determines whether the page should be skipped (shouldn't be shown) by wizard.
   *
   * @return {@code true} if this page should be skipped, otherwise - {@code false}
   */
  boolean canSkip();
}
