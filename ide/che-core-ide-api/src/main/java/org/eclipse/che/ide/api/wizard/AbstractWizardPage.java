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

/**
 * Abstract base implementation of a {@link WizardPage}.
 *
 * <p>It is completed and can not be skipped by default.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractWizardPage<T> implements WizardPage<T> {
  protected T dataObject;
  protected Map<String, String> context;
  protected Wizard.UpdateDelegate updateDelegate;

  /** Create wizard page. */
  protected AbstractWizardPage() {}

  /**
   * {@inheritDoc}
   *
   * <p>Sub-classes should invoke {@code super.init} at the beginning of this method.
   *
   * <p>Multiple pages have the same {@code dataObject}, and any change to the {@code dataObject}
   * made by one page is available to the other pages.
   */
  @Override
  public void init(T dataObject) {
    this.dataObject = dataObject;
  }

  /** {@inheritDoc} */
  @Override
  public void setContext(@NotNull Map<String, String> context) {
    this.context = context;
  }

  /** {@inheritDoc} */
  @Override
  public void setUpdateDelegate(@NotNull Wizard.UpdateDelegate delegate) {
    this.updateDelegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCompleted() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean canSkip() {
    return false;
  }
}
