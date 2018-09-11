/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.ide.project;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/** @author Kaloyan Raev */
@ImplementedBy(ComposerPageViewImpl.class)
public interface ComposerPageView extends View<ComposerPageView.ActionDelegate> {
  /** Returns value of the package attribute. */
  String getPackage();

  /** Sets value of the package attribute. */
  void setPackage(String value);

  /**
   * Marks a field with information about package as empty.
   *
   * @param doShow <code>true</code> to show wrong border, <code>false</code> to hide it
   */
  void showPackageMissingIndicator(boolean doShow);

  /**
   * Sets whether package field is enabled.
   *
   * @param isEnable <code>true</code> to enable the widget, <code>false</code> to disable it
   */
  void changePackageFieldState(boolean isEnable);

  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having changed on the fields. */
    void onAttributesChanged();
  }
}
